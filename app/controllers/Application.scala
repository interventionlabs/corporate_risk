package controllers

import play.api._
import play.api.mvc._
import deductions.runtime.html.{ CreationForm, TableView }
import deductions.runtime.services.FormSaver
import deductions.runtime.sparql_cache.RDFCache
import java.net.URLDecoder
import java.io.ByteArrayOutputStream
import org.xhtmlrenderer.pdf.ITextRenderer
import scalax.chart.api._

import models.{ UserData, ResponseAnalysis }
import Auth._

object Application extends Controller with Secured with RDFCache {
  lazy val tableView = new TableView {}
  import ops._

  def index = withUser { implicit user =>
    implicit request =>
      Ok(views.html.index(UserData.getUserData(user).map {
        case (uri, label) =>
          (fromUri(uri), label,
            models.ResponseAnalysis.responsesCount(user, fromUri(uri))
          )
      }))
  }

  /** edit given URI */
  def form(uri: String) = withUser { implicit user =>
    implicit request =>
      println("editURI: " + request)
      Ok(views.html.form(tableView.htmlForm(uri, editable = true
      //        lang = chooseLanguage(request)
      ).get))

  }

  /** create new instance of given class (unused) */
  def create(url: String) = withUser { implicit user =>
    implicit request =>
      Ok(views.html.form(new CreationForm { actionURI = routes.Application.save.url }.create(url).get))
  }

  def save = withUser { implicit user =>
    implicit request =>
      val uri = request.body match {
        case form: AnyContentAsFormUrlEncoded =>
          new FormSaver().saveTriples(form.data)
          form.data.getOrElse("uri", Seq()).headOption match {
            case Some(url) => URLDecoder.decode(url, "utf-8")
            case _ => throw new IllegalArgumentException
          }
        case _ => throw new IllegalArgumentException
      }

      Ok(views.html.report(ResponseAnalysis.averagePerForm(user, uri)));
  }

  def exportPDF = withUser { implicit user =>
    implicit request =>
      val renderer = new ITextRenderer
      val buffer = new ByteArrayOutputStream
      renderer.setDocumentFromString(views.html.pdfreport.render(user).toString)
      renderer.layout
      renderer.createPDF(buffer)
      Ok(buffer.toByteArray()).withHeaders(CONTENT_TYPE -> "application/pdf")
  }

  //TODO: handle security
  def chart(charttype: String) = Action {
    val content = charttype match {
      case "pie" => PieChart(Vector(("oui", 254), ("non", 167), ("NSPP", 88)))
      case "radar" => SpiderWebChart(Vector(("Sécurité", 4), ("Fiabilité", 1), ("Gouvernance", 4), ("Vitesse", 2), ("Solidité", 3)))
    }

    Ok(content.encodeAsPNG(320, 320)).withHeaders(CONTENT_TYPE -> "image/png")
  }

}
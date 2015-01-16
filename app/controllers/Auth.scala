package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.User
import models.InMemoryUser

/** TODO indiquer la source d'inspiration */
object Auth extends Controller {

  val loginForm = Form(
    tuple("email" -> text, "password" -> text) verifying
      ("Invalid email or password", result => result match {
        case (email, password) => check(email, password)
      })
  )

  def check(username: String, password: String) = {
    (username == "admin" && password == "1234")
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession(Security.username -> user._1)
    )
  }

  def register = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}

/** TODO comment. */
trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  /** TODO */
  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username =>
    implicit request =>
      InMemoryUser.find(username).map { user =>
        f(user)(request)
      }.getOrElse(onUnauthorized(request))
  }
}
package controllers

import model.{User, Users, Balance}
import play.api.mvc._
import play.api._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.json.JsPath
import play.api.libs.json.Json.toJson
import play.api.libs.json.Reads
import play.api.libs.json.Reads._
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.Reads.functorReads
import play.api.mvc.Result
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration

class Application extends Controller {
    
    def getUser(request: RequestHeader): String =
	{
		val x = request.session.get("user")
		if(x.isEmpty) null
		else x.head
	}
	
	def isLoggedIn(request: RequestHeader): Boolean =
	{
		getUser(request) != null
	}
	
    def index = Action { implicit request =>
        
        if(isLoggedIn(request)) {
            request.session.get("user").map {
                user =>
                {   
                    val p = Await.result(Users.getPrivilege(user), Duration(1, "seconds"))
                
                    if(p == 3) Ok(views.html.welcomeadmin())
                    else Ok(views.html.welcomeuser(user))
                }
            }.getOrElse(Redirect(routes.Application.index()))
        } else {
            Ok(views.html.index()).withNewSession
        }
    }
    
    def login = Action(parse.json) { implicit request =>

        implicit val loginRequest: Reads[LoginRequest] = Json.reads[LoginRequest]
        request.body.validate[LoginRequest] match { 
            case s: JsSuccess[LoginRequest] if (s.get.authenticate) => {
                Ok(toJson(Map("valid" -> true))).withSession("user" -> s.get.username)
            }
            // Not valid
            case _ => Ok(toJson(Map("valid" -> false)))
        }
    }
    
    def signup = Action(parse.json) { implicit request =>

        implicit val signupRequest: Reads[SignupRequest] = Json.reads[SignupRequest]
        
        request.body.validate[SignupRequest] match { 
            case s: JsSuccess[SignupRequest] if (s.get.authenticate) => {
                if(s.get.username == "admin") {
                    val new_cus = User(0, s.get.username, s.get.email, Users.hashPassword(s.get.password), 3, 1)
                    val new_cus_balance = Balance(0, s.get.username, 500)
                    
                    Users.add(new_cus, new_cus_balance)
                    Ok(toJson(Map("valid" -> true))).withSession("user" -> s.get.username)
                }else {
                    val new_cus = User(0, s.get.username, s.get.email, Users.hashPassword(s.get.password), 1, 1)
                    val new_cus_balance = Balance(0, s.get.username, 500)
                    Users.add(new_cus, new_cus_balance)
                    Ok(toJson(Map("valid" -> true))).withSession("user" -> s.get.username)
                }
            }
            // Not valid
            case _ => Ok(toJson(Map("valid" -> false)))
        }
    }
    
    def logout = Action { implicit request =>
        
        Redirect(routes.Application.index()).withNewSession
    }
    
    def welcome = Action { implicit request =>
        request.session.get("user").map {
            user =>
            {   
                val p = Await.result(Users.getPrivilege(user), Duration(1, "seconds"))
                
                if(p == 3) Ok(views.html.welcomeadmin())
                else Ok(views.html.welcomeuser(user))
            }
        }.getOrElse(Redirect(routes.Application.index()))
    }
    
    def add = Action(parse.json) { implicit request =>

        implicit val addRequest: Reads[SignupRequest] = Json.reads[SignupRequest]  // add form is exactly like sign up form
        
        request.body.validate[SignupRequest] match { 
            case s: JsSuccess[SignupRequest] if (s.get.authenticate) => {
                
                val new_cus = User(0, s.get.username, s.get.email, Users.hashPassword(s.get.password), 1, 1)
                val new_cus_balance = Balance(0, s.get.username, 500)
                 
                Users.add(new_cus, new_cus_balance)
                Ok(toJson(Map("valid" -> true))).withSession("user" -> "admin")
            }
            // Not valid
            case _ => Ok(toJson(Map("valid" -> false)))
        }
    }
    
    def delete = Action(parse.json) { implicit request =>
        
        implicit val deleteRequest: Reads[DeleteRequest] = Json.reads[DeleteRequest]
        
        request.body.validate[DeleteRequest] match { 
            case s: JsSuccess[DeleteRequest] if (s.get.authenticate) => {
                Users.delete(s.get.username)
                Ok(toJson(Map("valid" -> true))).withSession("user" -> "admin")
            }
            case _ => {
                Ok(toJson(Map("valid" -> false)))
            }
        }
        
    }
    
    def block = Action(parse.json) { implicit request =>
    
        implicit val blockRequest: Reads[BlockRequest] = Json.reads[BlockRequest]
        
        request.body.validate[BlockRequest] match {
            case s: JsSuccess[BlockRequest] if (s.get.authenticate) => {
                Users.block(s.get.username)
                Ok(toJson(Map("valid" -> true))).withSession("user" -> "admin")
            }
            case _ => {
                Ok(toJson(Map("valid" -> false)))
            }
        }
    }
    
    def checkBalance = Action { implicit request =>
    
        val x = request.session.get("user")
        val p = Users.getUser(x.head)
        //print(x.head)
     
        val m = Await.result( p.map {
            case Some(u) => Users.checkBalance(u.id)
            case None => 0
        }, Duration(1, "seconds") )
        
        //m map {
          //  case i => Ok("Your account: " + i)
            //case 0 => Ok("Your account: 0")
        //}
        Ok("Your account: " + m)
    }
    
}

case class LoginRequest(username: String, password: String) {
    def authenticate = Users.checkLogInValid(username, password)
}

case class SignupRequest(username: String, email: String, password: String) {
    def authenticate = Users.checkSignUpValid(username, email)
}

case class DeleteRequest(username: String) {
    def authenticate = Users.checkExist(username)
}

case class BlockRequest(username: String) {
    def authenticate = Users.checkExist(username)
}
package model

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import org.mindrot.jbcrypt.BCrypt
import scala.concurrent.Awaitable
import scala.concurrent.duration._

case class User(id: Int, username: String, email: String, password: String, privilege: Int, flag: Int)
case class Balance(id: Int, username: String, money: Int)

class UserTableDef(tag: Tag) extends Table[User](tag, "user") {

  def id = column[Int]("id", O.PrimaryKey,O.AutoInc)
  def username = column[String]("username")
  def email = column[String]("email")
  def password = column[String]("password")
  def privilege = column[Int]("privilege")
  def flag = column[Int]("flag")
  
  override def * =
    (id, username, email, password, privilege, flag) <>(User.tupled, User.unapply)
}

class BalanceTableDef(tag: Tag) extends Table[Balance](tag, "balance") {
    
    def id = column[Int]("id", O.PrimaryKey,O.AutoInc)
    def username = column[String]("username")
    def money = column[Int]("money")
    
    override def * = 
        (id, username, money) <>(Balance.tupled, Balance.unapply)
}

object Users {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val users = TableQuery[UserTableDef]          //query user table
  val balance = TableQuery[BalanceTableDef]     //query balance table
  
  def checkBalance(id: Int): Int = {
      val q = dbConfig.db.run(balance.filter(a => a.id === id).result.headOption)
      Await.result( q.map {
          case Some(u) => u.money
          case None => -1
      }, Duration(1, "seconds") )
  }
  
  def checkLogInValid(username: String, password: String): Boolean = {
  
    val q = dbConfig.db.run {
        users.filter(_.username === username).result.headOption
    } map {
        case Some(u) if (BCrypt.checkpw(password, u.password) && (u.flag == 1)) => true
        case _ => false
    }
    
    return Await.result(q, Duration(1, "seconds"))
  }
  
  def checkSignUpValid(username: String, email: String): Boolean = {
  
    val q = dbConfig.db.run(users.filter(a => a.username === username || a.email === email).exists.result)
    
    return !(Await.result(q, Duration(1, "seconds")))
  }
  
  def checkExist(username: String): Boolean = {
      
      val q = dbConfig.db.run(users.filter(a => a.username === username).exists.result)
      
      return (Await.result(q, Duration(1, "seconds")))
  }
  
  def add(user: User, bal: Balance): Future[Int] = {
    
    //println(user.password)
    
    dbConfig.db.run(users += user)
    dbConfig.db.run(balance += bal)
  }
  
  def hashPassword(password: String): String = {
    return BCrypt.hashpw(password, BCrypt.gensalt(12))
  }
  
  def delete(username: String): Future[Int] = {
     dbConfig.db.run(users.filter(_.username === username).delete)
  }
  
  def block(username: String): Future[Int] = {
     val q = for(u <- users if u.username === username) yield u.flag
     dbConfig.db.run(q.update(0))
  }
  
  def getUser(username: String): Future[Option[User]] = {
     dbConfig.db.run(users.filter(_.username === username).result.headOption)
  }
 
  def getPrivilege(username: String): Future[Int] = {
	val m = dbConfig.db.run(users.filter(_.username === username).result.headOption)
		
	  m.map {
		case Some(u) => u.privilege
		case None => -1
	  }
	}
}
 
  
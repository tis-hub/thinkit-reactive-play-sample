package controllers.sample

import java.sql.Date
import javax.inject.{Inject, Singleton}

import forms.sample.EventSearchForm
import models.Tables.{Event, EventRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

@Singleton
class EventSearch @Inject()(val messagesApi: MessagesApi)(dbConfigProvider: DatabaseConfigProvider) extends Controller with I18nSupport with EventSearchForm with HasDatabaseConfig[JdbcProfile] {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  /**
    * EventSearchの初期表示
    */
  def index = Action { implicit request =>
    Ok(views.html.sample.eventSearch())
  }

  /**
    * 通常のHTTPリクエストに対するControllerの実装
    */
  def search1 = Action.async { implicit request =>
    // Eventの一覧（Seq[EventRow]）をFutureで取得
    val future: Future[Seq[EventRow]] = getEvents

    future.map { events =>
      Ok(views.html.sample.eventSearch(events))
    }
  }

  def getEvents = {
    db.run(Event.result)
  }

  /**
    * Reactive SlickがFutureを返す
    */
  def search2 = Action.async { implicit request =>
    // コレクションを扱うようにfilterでeventNmが「ScalaMatsuri」のデータのみに絞り込む
    val query = Event.filter(_.eventNm === "ScalaMatsuri")
    val action = query.result

    // db.runを実行するとFutureを返す
    val future: Future[Seq[EventRow]] = db.run(action)

    // 結果を返す
    future.map { events =>
      Ok(views.html.sample.eventSearch(events))
    }
  }

  /**
    * Reactive SlickがストリーミングのFutureを返す
    */
  def printByStream = {
    // 全Eventの一覧を取得
    val query = Event
    val action = query.result

    // Eventの一覧をDatabasePublisherで取得
    val dbPublisher: DatabasePublisher[EventRow] = db.stream(action)

    // 全イベント名を出力
    dbPublisher.foreach { event =>
      println(s"[printByStream] Event name: ${event.eventNm}")
    }
  }

  /**
    * データベース処理結果が返ってきた時の処理を定義する
    */
  def futureMap = {
    // 2016年1月30日のイベントを取得するクエリ
    val query = Event.filter(_.eventDate === Date.valueOf("2016-01-30"))
    val action = query.result

    // db.runを実行するとFutureを返す
    val future: Future[Seq[EventRow]] = db.run(action)

    val eventNms: Future[Seq[String]] = future.map { events =>
      // データベースからの応答が返ってきたら実行したい処理を定義
      events.map { event =>
        // イベント名を出力
        println(s"[futureMap] Event name: ${event.eventNm}")
        event.eventNm
      }
    }

    // データベースからの応答を待たずに実行したい処理を定義
    sendMail("futureMap")
  }

  /**
    * データベース処理の結果を待って処理をする
    */
  def futureAwait: Unit = {
    // 2016年1月30日のイベントを取得するクエリ
    val query = Event.filter(_.eventDate === Date.valueOf("2016-01-30"))
    val action = query.result

    // db.runを実行するとFutureを返す
    val future: Future[Seq[EventRow]] = db.run(action)

    // データベースからの応答が返って来るまで（Duration.Inf）待つ
    val events = Await.result(future, Duration.Inf)
    val eventNms: Seq[String] = events.map { event =>
      // イベント名を出力
      println(s"[futureAwait] Event name: ${event.eventNm}")
      event.eventNm
    }

    // データベースからの応答後に実行したい処理を定義
    sendMail(s"futureAwait:${eventNms.mkString(",")}")
  }

  /**
    * メールを送る（ダミー）
    */
  def sendMail(name: String) = {
    println(s"[${name}] Mail was sent")
  }

  /**
    * Stream、Future等のサンプル処理の呼び出し
    */
  def others = Action { implicit request =>
    println("-- printByStream --")
    printByStream
    println("-- futureMap --")
    futureMap
    println("-- futureAwait --")
    futureAwait
    Redirect(controllers.sample.routes.EventSearch.index)
  }

}

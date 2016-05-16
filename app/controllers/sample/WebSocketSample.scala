package controllers.sample

import javax.inject.{Inject, Singleton}

import actor.HelloActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

@Singleton
class WebSocketSample  @Inject()(val messagesApi: MessagesApi)(implicit system: ActorSystem, materializer: Materializer) extends Controller with I18nSupport  {
  /**
   * WebSocketの初期表示
   *
   */
  def index = Action { implicit request =>
    Ok(views.html.sample.webSocket())
  }

  /**
   * WebSocketリクエストに対するControllerの実装
   */
  def hi = WebSocket.using[String] { request =>
    // 受信メッセージは無視して、「Hi!」という文字列を返す
    val in = Iteratee.ignore[String]
    val out = Enumerator("Hi!").andThen(Enumerator.eof)
    (in, out)
  }

  /**
   * WebSocketリクエストに対するActorを用いるControllerの実装
   */
  def hello = WebSocket.accept[String, String] { request =>
    // HelloActorの生成
    ActorFlow.actorRef[String, String] { out =>
      HelloActor.props(out)
    }
  }
}

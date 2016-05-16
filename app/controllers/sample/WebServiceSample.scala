package controllers.sample

import javax.inject.{Inject, Singleton}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.ws._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class WebServiceSample @Inject()(val messagesApi: MessagesApi)(ws: WSClient) extends Controller  with I18nSupport  {
  /**
   * WebServiceの初期表示
   */
  def index = Action { implicit request =>
    Ok(views.html.sample.webService())
  }

  /**
   * Webサービスを呼び出す
   */
  def example = Action.async { implicit request =>
    // Webサービスの呼び出し
    val wsRequest: WSRequest = ws.url("http://example.com/")
    val future: Future[WSResponse] = wsRequest.get()

    future.map { response =>
      Ok(response.body).as(HTML)
    }
  }
}

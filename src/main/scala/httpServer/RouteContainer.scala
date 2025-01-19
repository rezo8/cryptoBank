package httpServer

import zio.*
import zio.http.{Routes, *}

trait RouteContainer {
  def routes: Routes[Any, Response]
}

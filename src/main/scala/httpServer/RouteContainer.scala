package httpServer

import zio.http.{Route, Routes}

trait RouteContainer {
  def routes: Iterable[Route[Any, Exception]]
}

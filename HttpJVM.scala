// //> using lib "com.softwaremill.sttp.client3::fs2::3.6.2"
// //> using platform "jvm"

// import cats.effect.IO
// import org.legogroup.woof.Logger
// import sttp.client3.httpclient.fs2.HttpClientFs2Backend

// def httpBackend(using Logger[IO]) = HttpClientFs2Backend.resource[IO]()

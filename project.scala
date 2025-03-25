//> using scala "3"
//> using dep "org.typelevel::cats-effect::3.6.0"
//> using dep "co.fs2::fs2-core::3.12.0"
//> using dep "co.fs2::fs2-io::3.12.0"
//> using dep "com.softwaremill.sttp.client4::circe::4.0.0-RC2"
//> using dep "com.softwaremill.sttp.client4::cats::4.0.0-RC2"
//> using dep "io.chrisdavenport::rediculous::0.5.1"
//> using dep "io.circe::circe-core::0.14.12"
//> using dep "io.circe::circe-parser::0.14.12"
//> using dep "org.legogroup::woof-core::0.7.0"
//> using dep "com.monovore::decline-effect::2.5.0"
//> using dep "eu.timepit::fs2-cron-cron4s:0.10.0"

//> using jsModuleKind "es"
//> using jsAvoidClasses false
//> using jsAvoidLetsAndConsts false
//> using jsAllowBigIntsForLongs true
//> using jsEsVersionStr "es2021"

//> using option -Wunused:all
//> using option -Wvalue-discard
//> using option -deprecation

//> using resourceDir ./resources

// Native image version supports max of 17
//> using option -release 17

//> using scala "3"
//> using dep "org.typelevel::cats-effect::3.5.4"
//> using dep co.fs2::fs2-core::3.10.2
//> using dep co.fs2::fs2-io::3.10.2
//> using dep "com.softwaremill.sttp.client4::circe::4.0.0-M11"
//> using dep "com.softwaremill.sttp.client4::cats::4.0.0-M11"
//> using dep "io.circe::circe-core::0.14.6"
//> using dep "io.circe::circe-parser::0.14.6"
//> using dep "org.legogroup::woof-core::0.7.0"
//> using dep com.monovore::decline-effect::2.4.1

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

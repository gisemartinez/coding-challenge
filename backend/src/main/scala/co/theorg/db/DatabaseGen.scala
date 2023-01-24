package co.theorg.db

object DatabaseGen extends App {

  slick.codegen.SourceCodeGenerator.main(
    Array(
      "slick.jdbc.PostgresProfile",
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5438/postgres",
      "./src/main/scala",
      "co.theorg.db",
      "postgres",
      "postgres"
    )
  )
}

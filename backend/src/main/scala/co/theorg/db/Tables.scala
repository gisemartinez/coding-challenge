package co.theorg.db

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends Tables {
  val profile = slick.jdbc.PostgresProfile
}

/** Slick data model trait for extension, choice of backend or usage in the cake
  * pattern. (Make sure to initialize this late.)
  */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = ChartNode.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table ChartNode
    * @param id
    *   Database column id SqlType(uuid), PrimaryKey
    * @param parentid
    *   Database column parentid SqlType(uuid), Default(None)
    * @param title
    *   Database column title SqlType(varchar)
    * @param level
    *   Database column level SqlType(int4)
    */
  case class ChartNodeRow(
      id: java.util.UUID,
      parentid: Option[java.util.UUID] = None,
      title: String,
      level: Int
  )

  /** GetResult implicit for fetching ChartNodeRow objects using plain SQL
    * queries
    */
  implicit def GetResultChartNodeRow(implicit
      e0: GR[java.util.UUID],
      e1: GR[Option[java.util.UUID]],
      e2: GR[String],
      e3: GR[Int]
  ): GR[ChartNodeRow] = GR { prs =>
    import prs._
    ChartNodeRow.tupled(
      (<<[java.util.UUID], <<?[java.util.UUID], <<[String], <<[Int])
    )
  }

  /** Table description of table chart_node. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class ChartNode(_tableTag: Tag)
      extends profile.api.Table[ChartNodeRow](_tableTag, "chart_node") {
    def * =
      (id, parentid, title, level).<>(ChartNodeRow.tupled, ChartNodeRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      ((Rep.Some(id), parentid, Rep.Some(title), Rep.Some(level))).shaped.<>(
        { r =>
          import r._;
          _1.map(_ => ChartNodeRow.tupled((_1.get, _2, _3.get, _4.get)))
        },
        (_: Any) =>
          throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)

    /** Database column parentid SqlType(uuid), Default(None) */
    val parentid: Rep[Option[java.util.UUID]] =
      column[Option[java.util.UUID]]("parentid", O.Default(None))

    /** Database column title SqlType(varchar) */
    val title: Rep[String] = column[String]("title")

    /** Database column level SqlType(int4) */
    val level: Rep[Int] = column[Int]("level")

    /** Foreign key referencing ChartNode (database name chart_node_fk) */
    lazy val chartNodeFk = foreignKey("chart_node_fk", parentid, ChartNode)(
      r => Rep.Some(r.id),
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction
    )
  }

  /** Collection-like TableQuery object for table ChartNode */
  lazy val ChartNode = new TableQuery(tag => new ChartNode(tag))
}

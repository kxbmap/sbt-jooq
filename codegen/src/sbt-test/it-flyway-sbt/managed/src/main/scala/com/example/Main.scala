package com.example

import com.example.db.Tables
import org.jooq.SQLDialect
import org.jooq.impl.DSL

object Main extends App {

  val b = Tables.BOOK.as("b")
  val a = Tables.AUTHOR.as("a")
  val l = Tables.LANGUAGE.as("l")
  val bs = Tables.BOOK_STORE.as("bs")
  val b2bs = Tables.BOOK_TO_BOOK_STORE.as("b2bs")

  val query = DSL.using(SQLDialect.H2)
    .select(
      b.TITLE.as("Title"),
      DSL.concat(a.FIRST_NAME, DSL.inline(" "), a.LAST_NAME).as("Author"),
      l.CD.as("Language"),
      bs.NAME.as("Book Store"),
      b2bs.STOCK.as("Stock"),
    )
    .from(b)
    .join(a).on(b.AUTHOR_ID.equal(a.ID))
    .join(l).on(b.LANGUAGE_ID.equal(l.ID))
    .join(b2bs).on(b.ID.equal(b2bs.BOOK_ID))
    .join(bs).on(bs.NAME.equal(b2bs.NAME))

  println(query)

}

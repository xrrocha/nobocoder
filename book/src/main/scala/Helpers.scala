
import scala.util.Try
import scalatags.Text.TypedTag
import scalatags.Text.all._
import scalatags.generic
import scalatags.text.Builder

object Helpers {
  object sect extends scalatex.site.Section()

  object hl extends scalatex.site.Highlighter{
    def suffixMappings = Map(
      "scala" -> "scala",
      "xtend" -> "xtend",
      "java" -> "java",
      "sbt" -> "scala"
    )
  }

  def lnk(text: String, url: String) = {
    if(url.contains("://"))
      a(text, href:=url)
    else
      a(text, href:=s"http://$url")
  }

  def dataTable(dataString: String, separator: String = ",", headerSeparator: String = "\\|") = {
    val lines = dataString
      .trim
      .split("\\r?\n")
      .map(_.trim.split(separator))

    case class Header(parts: Array[String]) {
      val title = parts(0)
      val alignment =
        if (parts.length == 1) "left"
        else parts(1).toLowerCase.headOption.getOrElse("l") match {
          case 'r' => "right"
          case 'c' => "center"
          case _ => "left"
        }
    }
    val headers = lines.head.map(h => Header(h.split(headerSeparator)))

    val data = lines.tail

    table(
      thead(tr(headers.map(h => th(h.title)))),
      tbody(
        data.map { row =>
          tr(
            row.zip(headers).map { case(cell, header) =>
              td(textAlign := header.alignment, cell)
            }
          )
        }
      )
    )
  }
}

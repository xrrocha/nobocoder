
import scala.util.Try
import scalatags.Text.TypedTag
import scalatags.Text.all._
import scalatags.{Text, generic}
import scalatags.generic.{Attr, AttrPair}
import scalatags.text.Builder

object Helpers {
  object sect extends scalatex.site.Section()

  object hl extends scalatex.site.Highlighter {
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

  val lang = "lang".attr
  sealed class Language(val name: String, val order: Int)
  case object java extends Language("Java", 1)
  case object scala extends Language("Scala", 2)
  case object xtend extends Language("Xtend", 3)
  object Language {
    val values = Seq(java, scala, xtend)
      .map(l => l.name.toLowerCase -> l)
      .toMap
    def apply(name: String) = values(name)
  }

  var snippetCount = 0

  def multiSnippet(frags: Frag*) = {
   val snippets = frags
      .filter(_.isInstanceOf[TypedTag[String]])
      .map(_.asInstanceOf[TypedTag[String]].modifiers)
      .map { (pair: List[Seq[Text.Modifier]]) =>
        val children = pair(0)
          .filter(_.isInstanceOf[TypedTag[String]])
          .map(_.asInstanceOf[TypedTag[String]])
          .map(ttag => (ttag.tag, ttag))
          .toMap
        val attributes = pair(1).map { case AttrPair(Attr(name), value, _) =>
          (name, value.toString)
        }.toMap
        (children, attributes)
      }
      .sortBy { case(children, attributes) =>
        Language(attributes("lang")).order
      }

    snippetCount += 1

    val tabs = ul(
      snippets.map { case(children, attributes) =>
        li(
          a(
            href := s"#snippet-${attributes("lang")}-$snippetCount",
            img(src := s"img/${attributes("lang")}-icon-16.png"),
            raw("&#160;"),
            Language.values(attributes("lang")).name
          )
        )
      }
    )

    val divs = snippets.map { case(children, attributes) =>
      div(
        id := s"snippet-${attributes("lang")}-$snippetCount",
        lang := attributes("lang"),
        pre({
          val lines = children("code")
            .modifiers
            .flatten
            .filter(_.isInstanceOf[StringFrag])
            .map { case StringFrag(line) => line }
            .mkString("\n")
            .split("\\r?\\n")
            .toList
          val textLines = lines match {
            case line :: rest if line.trim.length == 0 => rest
            case _ => lines
          }
          val result =
            if (textLines.length == 0) Seq()
            else {
              val prefix = textLines.head.takeWhile(Character.isWhitespace)
              textLines.map("  " + _.substring(prefix.length))
            }

          code(
            cls := s"language-${attributes("lang")}",
            result.mkString("\n")
          )
        }),
        hr(),
        children.getOrElse("div", div(cls := "snippet-comment"))
      )
    }

    div(
      div(id := s"snippet-$snippetCount",tabs, divs),
      script(s"$$( '${s"#snippet-$snippetCount"}' ).tabs();")
    )
  }

  def snippet(language: Language)(frags: Frag*) = {
    div(lang := language.toString, frags)
  }

  def comment(frags: Frag*) = div(cls := "snippet-comment", frags)
}

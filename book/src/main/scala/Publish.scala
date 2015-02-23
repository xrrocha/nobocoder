
import ammonite.all.{rel => _, _}
import ammonite.ops.{FileType, Path}

import scalatags.Text.all._

object Publish {
  def main(args: Array[String]): Unit = {
    val wd = ammonite.all.processWorkingDir

    val site = new scalatex.site.Site {
      def content = Map("index.html" -> index())

      override def headFrags: Seq[Frag] = super.headFrags ++ Seq(
        meta(charset := "UTF-8"),
        link(href := "css/book.css", rel := "stylesheet"),
        link(href := "http://code.jquery.com/ui/1.11.3/themes/smoothness/jquery-ui.css", rel := "stylesheet"),
        script(src:= "http://code.jquery.com/jquery-1.10.2.js"),
        script(src:= "http://code.jquery.com/ui/1.11.3/jquery-ui.js")
      )

      override def bundleResources(outputRoot: Path) = {
        super.bundleResources(outputRoot)

        val resourceDir = wd/'src/'main/'resources
        val localResources = (ls.rec! resourceDir |? (_.fileType == FileType.File))
        for (res <- localResources) {
          read.bytes! res |> write.over! outputRoot/(res - resourceDir)
        }
      }

    }

    site.renderTo(wd/'target/'site)

    println("Done")
  }
}

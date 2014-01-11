import scala.collection.mutable

/**
 * ScalaTags is a small XML/HTML construction library for Scala. See
 * [[https://github.com/lihaoyi/scalatags the Github page]] for an introduction
 * and documentation.
 */
package object scalatags {

  /**
   * Convenience object to help import all Tags, Attributes, Styles and
   * Datatypes into the global namespace via `import scalatags.all._`
   */
  object all extends Attrs with Styles with Tags with DataConverters
  /**
   * Convenience object to help import all Tags, and DataConverters, while
   * aliases Attributes as `attr` and Styles as `css`. Used via
   * `import scalatags.short._`
   */
  object short extends Tags with DataConverters{
    val attr = new Attrs{}
    val css = new Styles{}
  }
  /**
   * Mark the given string as "raw", meaning it will not get escaped when the
   * Scalatags fragment is serialized. This makes it easy to open up XSS holes
   * and other vulnerabilities, but is sometimes necessary and useful.
   */
  def raw(s: String) = new RawNode(s)

  /**
   * Allows you to modify a HtmlNode by adding a String to its list of children
   */
  implicit class StringNested(v: String) extends Nested{
    def build(children: mutable.Buffer[Node], attrs: mutable.Map[String, String]) = {
      children.append(new StringNode(v))
    }
  }


  /**
   * Allows you to modify a HtmlNode by adding a Seq containing other nest-able
   * objects to its list of children.
   */
  implicit class SeqNested[A <% Nested](xs: Seq[A])extends Nested{
    def build(children: mutable.Buffer[Node], attrs: mutable.Map[String, String]) = {
      for(x <- xs) x.build(children, attrs)
    }
  }

  /**
   * Provides extension methods on strings to fit them into HtmlNode fragments.
   */
  implicit class ExtendedString(s: String){
    /**
     * Converts the string to a HtmlTag
     */
    def tag = new HtmlTag(s)
    /**
     * Converts the string to a void HtmlTag; that means that they cannot
     * contain any content, and can be rendered as self-closing tags.
     */
    def voidTag = new HtmlTag(s, void=true)
    /**
     * Converts the string to a Attr
     */
    def attr = new Attr(s)
    /**
     * Converts the string to a TypedAttr; also takes the type of the TypedAttr
     */
    def attrTyped[T] = new TypedAttr[T](s)
    /**
     * Converts the string to a Style. The string is used as the cssName of the
     * style, and the jsName of the style is generated by converted the dashes
     * to camelcase.
     */
    def style = new TypedStyle[String](camelCase(s), s)
    /**
     * Converts the string to a TypedStyle; also takes the type of the
     * TypedStyle. The string is used as the cssName of the style, and the
     * jsName of the style is generated by converted the dashes to camelcase.
     */
    def styleTyped[T] = new TypedStyle[T](camelCase(s), s)
    /**
     * Converts the string to a CSS Cls object.
     */
    def cls = new Cls(s)
  }
  private[scalatags] def camelCase(dashedString: String) = {
    val first :: rest = dashedString.split("-").toList
    (first :: rest.map(s => s(0).toUpper + s.drop(1))).mkString
  }
}
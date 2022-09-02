package domain

class Answer(val id: String, val text: String, val tags: Set[Tag]) extends Serializable {
  def addTag(tag: Tag) = new Answer(id, text, tags + tag)
  def removeTag(tag: Tag) = new Answer(id, text, tags - tag)
}

object Answer {
  def apply(id: String, text: String, tags: Set[Tag]): Answer = {
    new Answer(id, text, tags)
  }
}

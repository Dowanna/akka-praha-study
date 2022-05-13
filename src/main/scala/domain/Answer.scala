package domain

class Answer(val id: String, val text: String, val tags: Set[Tag]) {
  def addTag(tag: Tag) = new Answer(id, text, tags + tag)
  def removeTag(tag: Tag) = new Answer(id, text, tags - tag)
}

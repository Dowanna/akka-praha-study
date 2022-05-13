package domain

class Question(val id: String, val title: String, val body: String, val answers: Set[Answer], val tags: Set[Tag]) {
  def addAnswer(answer: Answer) = new Question(id, title, body, answers + answer, tags)
  def addTag(tag: Tag) = new Question(id, title, body, answers, tags + tag)
  def removeTag(tag: Tag) = new Question(id, title, body, answers, tags - tag)
}

package domain

class Question(val id: String, val title: String, val body: String, val answers: Set[Answer], val tags: Set[Tag]) {
  // バリデーションのパターン
  // 1: Try
  // 2: Either
  // 3: Validation
  // 4: Option
  // 5: OptionT
  // 6: Reader
  // 7: Writer
  // 8: State
  def addAnswer(answer: Answer) = new Question(id, title, body, answers + answer, tags)
  def addTag(tag: Tag) = new Question(id, title, body, answers, tags + tag)
  def removeTag(tag: Tag) = new Question(id, title, body, answers, tags - tag)
}

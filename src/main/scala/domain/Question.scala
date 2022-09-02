package domain

case class AddAnswerError(tooManyAnswers: Boolean)

class Question private (val id: String, val title: String, val body: String, val answers: Set[Answer], val tags: Set[Tag])
    extends Serializable {
  // バリデーションのパターン
  // 1: Try
  // 2: Either
  // 3: Validation
  // 4: Option
  // 5: OptionT
  // 6: Reader
  // 7: Writer
  // 8: State
  def addAnswer(answer: Answer): Either[AddAnswerError, Question] = Question(id, title, body, answers + answer, tags)
  def addTag(tag: Tag) = new Question(id, title, body, answers, tags + tag)
  def removeTag(tag: Tag) = new Question(id, title, body, answers, tags - tag)
}

object Question {
  def apply(id: String, title: String, body: String, answers: Set[Answer], tags: Set[Tag]): Either[AddAnswerError, Question] = {
    if (answers.size >= 2) return Left(AddAnswerError(tooManyAnswers = true))

    Right(new Question(id, title, body, answers, tags))
  }
}

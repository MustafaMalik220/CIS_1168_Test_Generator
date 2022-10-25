// Mustafa Malik
// June 16, 2022
// Test Generator
//
// This program creates a Test object with an array of randomly selected ObjectiveQuestion
// objects from the TestBank Object. Then writes the test and answers to files (enter file names at run time)
// ** Change numQuestions (line 14) to any integer between 0 and the number of questions in Question_Bank.txt (12) **
//
import java.io.*;
import java.util.*;
public class TestDriver {
    public static void main(String[] args) throws FileNotFoundException{
        TestBank tb = new TestBank("Question_Bank.txt");
        int numQuestions = 3; // [0,12]
        Test randomTest = tb.randomTest(numQuestions);
        fileWriter(randomTest);
    }
    public static void fileWriter(Test test) throws FileNotFoundException{
        Scanner console = new Scanner(System.in);

        System.out.println("Enter file name for test:");
        String testFile = console.next();
        System.out.println("Enter file name for answer key:");
        String answerKeyFile = console.next();
        
        PrintStream testFilePS = new PrintStream(testFile);
        testFilePS.print(test);

        PrintStream answerKeyFilePS = new PrintStream(answerKeyFile);
        answerKeyFilePS.print(test.toString(true));

        console.close();
        testFilePS.close();
        answerKeyFilePS.close();
    }
}

class Question {
    protected int points;    
    protected int difficulty;
    protected final int MAX_DIFFICULTY = 5;
    protected final int MIN_DIFFICULTY = 1;
    protected int answerSpace;
    protected String questionText;  

    public Question(int points, int difficulty, int answerSpace, String questionText) {
        if (difficulty<MIN_DIFFICULTY || difficulty>MAX_DIFFICULTY) {
            throw new IllegalArgumentException("'difficulty' argument out of range for Question object.");
        }
        this.points = points;
        this.difficulty = difficulty;
        this.answerSpace = answerSpace;
        this.questionText = questionText;
    }

    @Override
    public String toString() {
        return questionText + " (" + points + " Points) (Difficulty: " + difficulty + ")" + lines(answerSpace);
    }
    private String lines(int numLines) {
        String lines = "";
        for (int i=0; i<numLines; i++) {
            lines += "\n";
        }
        return lines;
    }
    public int getPoints() {
        return points;
    }
}
class ObjectiveQuestion extends Question {
    protected String correctAnswer;
    public ObjectiveQuestion(int points, int difficulty, int answerSpace, String questionText, String correctAnswer) {
        super(points, difficulty, answerSpace, questionText);
        this.correctAnswer = correctAnswer;
    }
    public String toString(boolean withAnswer) {
        if (withAnswer) {
            return questionText + " (" + points + " Points) (Difficulty: " + difficulty + ")" + "\n\n\t" + correctAnswer;
        } else {
            return super.toString();
        }
    }
}
class FillInTheBlankQuestion extends ObjectiveQuestion {
    public FillInTheBlankQuestion(int points, int difficulty, int answerSpace, String questionText, String correctAnswer) {
        super(points, difficulty, answerSpace, questionText, correctAnswer);
        if (!questionText.contains("%s")) {
            throw new IllegalArgumentException("invalid format for 'questionText' argument in FillInTheBlankQuestion object.");
        }
    }
    @Override
    public String toString() {
        return String.format(super.toString(), blank());
    }
    @Override
    public String toString (boolean withAnswer) {
        if (withAnswer) {
            return String.format(super.toString(), correctAnswer);
        } else {
            return toString();
        }
    }
    private String blank() {
        String blank = "";
        for (int i=0; i<correctAnswer.length(); i++) {
            blank += "_";
        }
        return blank;
    }
}
class MultipleChoiceQuestion extends ObjectiveQuestion {
    protected String[] possibleAnswers;
    protected int correctAnswerIndex;
    public MultipleChoiceQuestion(int points, int difficulty, int answerSpace, String questionText, String[] possibleAnswers, int correctAnswerIndex) {
        super(points, difficulty, answerSpace, questionText, possibleAnswers[correctAnswerIndex]);
        this.possibleAnswers = possibleAnswers;
        this.correctAnswerIndex = correctAnswerIndex;
    }
    @Override
    public String toString() {
        return toString(false);
    }
    @Override
    public String toString(boolean withAnswer) {
        return super.toString() + answers(withAnswer);
    }
    private String answers(boolean withAnswer) {
        String answers = "";
        for (int i=0; i<possibleAnswers.length; i++) {
            if (withAnswer && i==correctAnswerIndex) {
                answers += "\n\n\t" + (i+1) + ". " + "**** " + possibleAnswers[i] + " ****";
            } else {
                answers += "\n\n\t" + (i+1) + ". " + possibleAnswers[i];
            }
        }
        return answers;
    }
}

class Test {
    ObjectiveQuestion[] questions;
    int totalPoints;
    public Test(ObjectiveQuestion[] questions) {
        this.questions = questions;
        
        totalPoints = 0;
        for (int i=0; i<questions.length; i++) {
            totalPoints += questions[i].points;
        }

    }
    public Test(ObjectiveQuestion[] questions, int totalPoints) {
        this.questions = questions;
        this.totalPoints = totalPoints;
    }
    public String toString() {
        return toString(false);
    }
    public String toString(boolean withAnswers) {
        String test = "Total points: "+totalPoints+"\n";

        for (int i=0; i<questions.length; i++) {
            test += "\nQuestion "+(i+1)+":\n";
            test += questions[i].toString(withAnswers);
            test += "\n";
        }
        return test;
    }
}

class TestBank {
    private ObjectiveQuestion[] Questions;
    public TestBank(String fileName) throws FileNotFoundException{
        File file = new File(fileName);

        Questions = new ObjectiveQuestion[numQuestions(file)];
        QuestionsFill(file);
    }
    private int numQuestions(File file) throws FileNotFoundException {
        Scanner fileScan = new Scanner(file);

        int numQuestions = 0;
        while (fileScan.hasNextLine()) {
            if (fileScan.nextLine().equals("****")) {
                numQuestions++;
            }
        }
        fileScan.close();
        return numQuestions;
    }
    private void QuestionsFill(File file) throws FileNotFoundException {
        Scanner fileScan = new Scanner(file);

        for (int i=0; i<Questions.length; i++) {
            while (!fileScan.nextLine().equals("****")) {continue;}
            String questionType = fileScan.next();

            int points = fileScan.nextInt();
            int difficulty = fileScan.nextInt();
            int answerSpace = fileScan.nextInt();
            fileScan.nextLine();
            String questionText = fileScan.nextLine();
            String correctAnswer;
            ObjectiveQuestion q; 
            if (questionType.equals("M")) {
                int numAnswers = fileScan.nextInt();
                fileScan.nextLine();
                String[] possibleAnswers = new String[numAnswers];
                for (int j=0; j<numAnswers; j++) {
                    possibleAnswers[j] = fileScan.nextLine();
                }
                int correctAnswerIndex = fileScan.nextInt();
                q = new MultipleChoiceQuestion(points, difficulty, answerSpace, questionText, possibleAnswers, correctAnswerIndex);
            } else if (questionType.equals("F")) {
                correctAnswer = fileScan.nextLine();
                q = new FillInTheBlankQuestion(points, difficulty, answerSpace, questionText, correctAnswer);
            } else {
                correctAnswer = fileScan.nextLine();
                q = new ObjectiveQuestion(points, difficulty, answerSpace, questionText, correctAnswer);
            }
            Questions[i] = q;
        }
        fileScan.close();
    }
    
    public ObjectiveQuestion[] getQuestions() {
        return Questions;
    }
    public Test randomTest(int numQuestions) {
        if (numQuestions > Questions.length || numQuestions < 0) {
            throw new IllegalArgumentException("invalid argument numQuestions for randomTest()");
        }
        ObjectiveQuestion[] q = new ObjectiveQuestion[numQuestions];

        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (int i=0; i<Questions.length; i++) {
            indexes.add(i);
        }

        Random rand = new Random();
        int bound = Questions.length;
        for(int i=0; i<numQuestions; i++) {
            int index = rand.nextInt(bound);
            q[i] = Questions[indexes.get(index)];
            indexes.remove(index);
            bound--;
        }
        
        Test test = new Test(q);
        return test;
    }
}

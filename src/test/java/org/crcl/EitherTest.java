package org.crcl;

import io.vavr.control.Either;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;

public class EitherTest {
    private final String[] SUBJECTS = {"MATH", "PHYSICS", "FRENCH", "ENGLISH", "CS"}; // can be fetched from db

    private static IntFunction<Student> buildStudent() {
        return index -> Student.withId(index) // create dummy students
                .withClass(new ClassRoom().setId("id_class").setName("CLASS 1"))
                .setFirstName("student_f_%".replace("%", String.valueOf(index)))
                .setLastName("student_l_%".replace("%", String.valueOf(index)))
                .setUsername(RandomUtils.nextBoolean() ? "student_u_%".replace("%", String.valueOf(index)) : null)
                .setEmail(RandomUtils.nextBoolean() ? "student_e_%@school.org".replace("%", String.valueOf(index)) : null);
    }

    @Test
    public void getEither() {
        IntStream.range(0, 10)
                .mapToObj(buildStudent())
                .map(this::buildDummyExams)
                .flatMap(List::stream)
                .map(this::processExams)
                // send success notification when process success.
                .map(this::doNotifyProcessSuccess)
                // send fail notification when process fail.
                .map(this::doNotifyProcessFail)
                .forEach(this::printResult);

    }

    private void printResult(Either<Pair<Exception, Exam>, Exam> exam) {
        String message;
        if (exam.isLeft()) {
            final Exam value = exam.getLeft().getValue();
            message = "process failed for "
                    + value.getStudent().getUsername()
                    + " subject " + value.getSubject()
                    + " reason "
                    + exam.getLeft().getLeft().getMessage();
        } else {
            final Exam value = exam.get();
            message = "process succeeded for "
                    + value.getStudent().getUsername()
                    + " subject " + value.getSubject();
        }
        System.out.println(message);

    }

    private Either<Pair<Exception, Exam>, Exam> doNotifyProcessSuccess(Either<Pair<Exception, Exam>, Exam> examEither) {
        // process success
        if (examEither.isRight()) {
            // do stuff ...
        }
        return examEither;
    }

    private Either<Pair<Exception, Exam>, Exam> doNotifyProcessFail(Either<Pair<Exception, Exam>, Exam> examEither) {
        // process fail
        if (examEither.isLeft()) {
            // do stuff ...
        }
        return examEither;
    }

    private Either<Pair<Exception, Exam>, Exam> processExams(Exam exam) {
        try {
            // processing logic goes here
            isValid(isNull(exam.getStudent().getUsername()), "Student username is not present");
            isValid(isNull(exam.getStudent().getEmail()), "Student email is not present");
            isValid(isNull(exam.getSubject()), "Subject not found");
            isValid(isNull(exam.getClassRoom()), "ClassRoom not found");
            isValid(exam.getMark() > 10 || exam.getMark() < 0, "Marks not acceptable");
            return Either.right(exam);
        } catch (Exception e) {
            return Either.left(Pair.of(e, exam));
        }
    }

    private static void isValid(boolean isValid, String reason) {
        if (isValid) throw new RuntimeException(reason);
    }


    private List<Exam> buildDummyExams(Student student) {
        return Arrays.stream(SUBJECTS)
                .map(buildExam(student))
                .toList();
    }

    private static Function<String, Exam> buildExam(Student student) {
        return subject -> Exam.of(student)
                .setClassRoom(student.getClassRoom())
                .setSubject(RandomUtils.nextBoolean() ? subject : null)
                .setMark(RandomUtils.nextInt(0, 10));
    }
}

@Data
@Accessors(chain = true)
class ClassRoom {
    private String id;
    private List<Student> students = new ArrayList<>();
    private String name;
}

@Getter
@Setter
@ToString
@Accessors(chain = true)
class Exam {
    @Setter(AccessLevel.PRIVATE)
    private Student student;
    private ClassRoom classRoom;
    private Integer mark;
    private String subject;

    private Exam() {
    }

    public static Exam of(Student student) {
        return new Exam().setStudent(student);
    }
}

@Getter
@Setter
@ToString
@Accessors(chain = true)
class Student {
    @Setter(AccessLevel.PRIVATE)
    private Integer id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    @Setter(AccessLevel.PRIVATE)
    private ClassRoom classRoom;

    private Student() {
    }

    public static Student withId(Integer id) {
        return new Student().setId(id);
    }

    public Student withClass(ClassRoom classRoom) {
        return this.setClassRoom(classRoom);
    }
}
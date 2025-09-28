import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

// ---------- ENTITY CLASSES ----------

public class ei {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        VirtualClassroomManager manager = VirtualClassroomManager.getInstance();

        System.out.println("Welcome to the Virtual Classroom Manager!");
        System.out.println("Commands: add_classroom, list_classrooms, remove_classroom, "
                + "add_student, list_students, schedule_assignment, submit_assignment, exit");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            String[] parts = input.split(" ", 3);
            String command = parts[0];

            try {
                switch (command) {
                    case "add_classroom":
                        manager.addClassroom(parts[1]);
                        break;
                    case "list_classrooms":
                        manager.listClassrooms();
                        break;
                    case "remove_classroom":
                        manager.removeClassroom(parts[1]);
                        break;
                    case "add_student":
                        manager.addStudent(parts[1], parts[2]);
                        break;
                    case "list_students":
                        manager.listStudents(parts[1]);
                        break;
                    case "schedule_assignment":
                        manager.scheduleAssignment(parts[1], parts[2]);
                        break;
                    case "submit_assignment":
                        String[] subParts = input.split(" ", 4);
                        manager.submitAssignment(subParts[1], subParts[2], subParts[3]);
                        break;
                    case "exit":
                        System.out.println("Exiting Virtual Classroom Manager. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid command.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}

class Student {
    private String id;
    private Set<String> enrolledClasses;

    public Student(String id) {
        this.id = id;
        this.enrolledClasses = new HashSet<>();
    }

    public String getId() { return id; }

    public void enrollClass(String className) {
        enrolledClasses.add(className);
    }

    public boolean isEnrolledIn(String className) {
        return enrolledClasses.contains(className);
    }
}

class Assignment {
    private String details;
    private Map<String, Boolean> submissions; // studentId -> submitted

    public Assignment(String details) {
        this.details = details;
        this.submissions = new HashMap<>();
    }

    public String getDetails() { return details; }

    public void submit(String studentId) {
        submissions.put(studentId, true);
    }

    public boolean isSubmitted(String studentId) {
        return submissions.getOrDefault(studentId, false);
    }
}

// ---------- MANAGER SINGLETON ----------

class Classroom {
    private String name;
    private Map<String, Student> students;
    private List<Assignment> assignments;

    public Classroom(String name) {
        this.name = name;
        this.students = new HashMap<>();
        this.assignments = new ArrayList<>();
    }

    public String getName() { return name; }
    public Map<String, Student> getStudents() { return students; }
    public List<Assignment> getAssignments() { return assignments; }

    public void addStudent(Student student) {
        students.put(student.getId(), student);
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }
}

// ---------- MAIN APPLICATION ----------

class VirtualClassroomManager {
    private static VirtualClassroomManager instance;
    private static final Logger logger = Logger.getLogger("VirtualClassroomLogger");
    public static VirtualClassroomManager getInstance() {
        if (instance == null) {
            instance = new VirtualClassroomManager();
        }
        return instance;
    }
    private Map<String, Classroom> classrooms;

    private Map<String, Student> students;

    private VirtualClassroomManager() {
        classrooms = new HashMap<>();
        students = new HashMap<>();
    }

    // ---------- CLASSROOM MANAGEMENT ----------
    public void addClassroom(String name) {
        if (classrooms.containsKey(name)) {
            logger.warning("Classroom already exists: " + name);
            return;
        }
        classrooms.put(name, new Classroom(name));
        System.out.println("Classroom " + name + " has been created.");
    }

    public void listClassrooms() {
        if (classrooms.isEmpty()) {
            System.out.println("No classrooms available.");
            return;
        }
        System.out.println("Available Classrooms:");
        classrooms.keySet().forEach(System.out::println);
    }

    public void removeClassroom(String name) {
        if (classrooms.remove(name) != null) {
            System.out.println("Classroom " + name + " removed.");
        } else {
            logger.warning("Attempted to remove non-existent classroom: " + name);
        }
    }

    // ---------- STUDENT MANAGEMENT ----------
    public void addStudent(String studentId, String className) {
        Classroom classroom = classrooms.get(className);
        if (classroom == null) {
            logger.warning("Classroom not found: " + className);
            return;
        }

        Student student = students.computeIfAbsent(studentId, Student::new);
        student.enrollClass(className);
        classroom.addStudent(student);

        System.out.println("Student " + studentId + " has been enrolled in " + className + ".");
    }

    public void listStudents(String className) {
        Classroom classroom = classrooms.get(className);
        if (classroom == null) {
            logger.warning("Classroom not found: " + className);
            return;
        }
        if (classroom.getStudents().isEmpty()) {
            System.out.println("No students enrolled in " + className);
            return;
        }
        System.out.println("Students in " + className + ":");
        classroom.getStudents().keySet().forEach(System.out::println);
    }

    // ---------- ASSIGNMENT MANAGEMENT ----------
    public void scheduleAssignment(String className, String details) {
        Classroom classroom = classrooms.get(className);
        if (classroom == null) {
            logger.warning("Classroom not found: " + className);
            return;
        }
        Assignment assignment = new Assignment(details);
        classroom.addAssignment(assignment);
        System.out.println("Assignment for " + className + " has been scheduled.");
    }

    public void submitAssignment(String studentId, String className, String details) {
        Classroom classroom = classrooms.get(className);
        if (classroom == null) {
            logger.warning("Classroom not found: " + className);
            return;
        }

        Optional<Assignment> assignment = classroom.getAssignments()
                .stream().filter(a -> a.getDetails().equals(details)).findFirst();

        if (assignment.isEmpty()) {
            logger.warning("Assignment not found for " + className);
            return;
        }

        Student student = students.get(studentId);
        if (student == null || !student.isEnrolledIn(className)) {
            logger.warning("Student not enrolled in " + className);
            return;
        }

        assignment.get().submit(studentId);
        System.out.println("Assignment submitted by Student " + studentId + " in " + className + ".");
    }
}

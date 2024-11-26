select student.name, student.age, faculty.name
from student
inner join faculty on student.faculty_id = faculty.id;

select student.name, student.age, avatar.data 
from student
right join avatar on avatar.student_id = student.id;
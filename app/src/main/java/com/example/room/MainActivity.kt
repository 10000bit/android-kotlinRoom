package com.example.room

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.room.databinding.ActivityMainBinding
import kotlinx.coroutines.runBlocking
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var myDao: MyDAO
    lateinit var myAdapter: MyAdapter

    private fun queryStudent(student_id: Int) {
        binding.editStudentId.setText(student_id.toString())
        runBlocking {
            val results = myDao.getStudentsWithEnrollment(student_id)
            if (results.isNotEmpty()) {
                val str = StringBuilder().apply {
                    append(results[0].student.id)
                    append("-")
                    append(results[0].student.name)
                    append(":")
                    for (c in results[0].enrollments) {
                        append(c.cid)
                        val cls_result = myDao.getClassInfo(c.cid)
                        if (cls_result.isNotEmpty())
                            append("(${cls_result[0].name})")
                        append(",")
                    }
                }
                binding.textQueryStudent.text = str
            } else {
                binding.textQueryStudent.text = ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // database setup
        myDao = MyDatabase.getDatabase(this).getMyDao()
        runBlocking {
            with(myDao) {
                insertStudent(Student(1, "james"))
                insertStudent(Student(2, "john"))
                insertClass(ClassInfo(1, "c-lang", "Mon 9:00", "E301", 1))
                insertClass(ClassInfo(2, "android prog", "Tue 9:00", "E302", 1))
                insertClass(ClassInfo(3, "OS", "Wed 9:00", "E303", 2))
                insertEnrollment(Enrollment(1, 1))
                insertEnrollment(Enrollment(1, 2))
            }
        }

        // recyclerview setup
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        myAdapter = MyAdapter(this, emptyList())
        myAdapter.setOnItemClickListener() {
            queryStudent(it)
        }
        binding.recyclerview.adapter = myAdapter

        val allStudents = myDao.getAllStudents()
        allStudents.observe(this) {
            myAdapter.updateList(it)
        }

        binding.queryStudent.setOnClickListener {
            val id = binding.editStudentId.text.toString().toInt()
            queryStudent(id)
        }

        binding.addStudent.setOnClickListener {
            if (binding.editStudentId.text.isNotEmpty() and binding.editStudentName.text.isNotEmpty()) {
                val id = binding.editStudentId.text.toString().toInt()
                val name = binding.editStudentName.text.toString()
                if (id > 0 && name.isNotEmpty()) {
                    runBlocking {
                        myDao.insertStudent(Student(id, name))
                    }
                }
            }
        }

        binding.deleteStudent.setOnClickListener {
            if (binding.editStudentId.text.isNotEmpty()) {
                val id = binding.editStudentId.text.toString().toInt()
                if (id > 0) {
                    runBlocking {
                        myDao.deleteStudent(Student(id, ""))
                    }
                }
            }
        }

        binding.enroll.setOnClickListener {
            if (binding.editStudentId.text.isNotEmpty()) {
                val id = binding.editStudentId.text.toString().toInt()
                if (id > 0) {
                    runBlocking {
                        val ret = myDao.checkStudentID(id)
                        if (ret > 0) {
                            /*
                            val random = Random()
                            val num = random.nextInt(2)
                            myDao.insertEnrollment(Enrollment(id, num))
                             */
                            myDao.insertEnrollment(Enrollment(id, 1))
                        }
                    }
                }
            }
        }

    }
}
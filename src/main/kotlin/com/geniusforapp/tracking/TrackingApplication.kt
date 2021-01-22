package com.geniusforapp.tracking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import javax.persistence.*


@SpringBootApplication
class TrackingApplication

fun main(args: Array<String>) {
    runApplication<TrackingApplication>(*args)
}

@RestController
@RequestMapping("users")
class UsersController(private val usersService: UsersService) {

    @GetMapping
    fun index(@RequestParam("page") page: Int, @RequestParam("size") size: Int): List<UserEntity> = usersService.getAllUsers(page, size)

    @PostMapping
    fun add(@RequestBody dtoUser: DtoUser): UserEntity = usersService.addUser(dtoUser)


    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Int): UserEntity = usersService.getUserById(id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: Int): UserEntity = usersService.deleteUser(id)

}

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such User")
data class UserNotFoundException(private val id: Int? = null) : RuntimeException("Cant find user record with id $id")


@Repository
interface UsersRepository : JpaRepository<UserEntity, Int>


@Service
class UsersService(private val usersRepository: UsersRepository) {

    fun getUserById(id: Int): UserEntity = usersRepository.findById(id)
            .orElseThrow { UserNotFoundException(id) }

    fun getAllUsers(page: Int = 1, size: Int = 10): List<UserEntity> = usersRepository
            .findAll(PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("id"))))
            .toList()

    fun addUser(dtoUser: DtoUser): UserEntity = usersRepository
            .save(UserEntity(username = dtoUser.username, email = dtoUser.email, password = dtoUser.password))


    fun deleteUser(id: Int): UserEntity = usersRepository.getOne(id)
            .copy(isDeleted = true)
            .also { usersRepository.save(it) }
}


data class DtoUser(val username: String, val email: String, val password: String)

@Entity
@Table
data class UserEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int? = null,
        val username: String = "",
        val email: String? = null,
        val isDeleted: Boolean? = false,
        private val userRole: UserRole? = UserRole.SUPER_ADMIN,
        private val password: String = ""
)

enum class UserRole {
    SUPER_ADMIN,
    ADMIN,
    DRIVER,
    CUSTOMER
}
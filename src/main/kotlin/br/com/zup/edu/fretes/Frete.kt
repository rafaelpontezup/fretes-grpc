package br.com.zup.edu.fretes

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.PositiveOrZero

@Entity
class Frete(
    @field:NotBlank
    @field:Pattern(regexp = "[0-9]{8}", message = "CEP com formato inválido")
    @Column(nullable = false, unique = true)
    val cep: String,

    @field:NotNull
    @field:PositiveOrZero
    @Column(nullable = false)
    val valor: BigDecimal = BigDecimal.ZERO
) {

    @Id
    @GeneratedValue
    val id: Long? = null

}

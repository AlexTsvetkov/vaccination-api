package com.pramod.vaccination.service

import com.pramod.vaccination.exception.VaccinationError
import com.pramod.vaccination.model.{VaccinationDetails, Vaccinations}
import zio.*
import zio.test.*
import zio.test.Assertion.*

import scala.collection.mutable.ListBuffer

object VaccinationServiceLiveTest extends ZIOSpecDefault {

  private val vaccinationList = ListBuffer(
    VaccinationDetails(1, "Pfizer", "USA"),
    VaccinationDetails(2, "Moderna", "Russia"),
    VaccinationDetails(3, "Sinopharm", "China")
  )

  val vaccinationServiceLayer: ZLayer[Any, Nothing, VaccinationRepository.Service with VaccinationService.Service] = VaccinationRepository.live >+> VaccinationService.live

  def spec = suite("VaccinationServiceLive")(

    test("updateVaccination method updates an existing vaccination with valid input") {
      val validVaccinationId = 2
      val updatedVaccinationDetails = VaccinationDetails(2, "Moderna", "USA")
      for {
        service <- ZIO.service[VaccinationService.Service]
        repo <- ZIO.service[VaccinationRepository.Service]
        result <- service.updateVaccination(validVaccinationId, updatedVaccinationDetails).map(_.vaccinationList)
      } yield assertTrue(result == repo.getVaccinations.toList)
    },

    test("getAllVaccination should return all vaccinations") {
      for {
        service <- ZIO.service[VaccinationService.Service]
        vaccinations <- service.getAllVaccination()
      } yield assert(vaccinations.vaccinationList)(hasSameElements(service.getVaccinationList.toList))
    },

    test("getVaccinationById should return correct vaccination for valid vaccinationId") {
      val vaccinationId = 1
      for {
        service <- ZIO.service[VaccinationService.Service]
        vaccination <- service.getVaccinationById(vaccinationId)
      } yield assert(vaccination.vaccinationId)(equalTo(vaccinationId))
    },

    test("getVaccinationById should fail with NotFound error for invalid vaccinationId") {
      val vaccinationId = 100
      for {
        service <- ZIO.service[VaccinationService.Service]
        result <- service.getVaccinationById(vaccinationId).exit
      } yield assert(result)(
        fails(equalTo(VaccinationError.NotFound(s"Vaccination not found for $vaccinationId"))))
    },

    test("Example of testing for expected failure") {
      for {
        result <- ZIO.fail("failureResult").exit
      } yield assert(result)(
        fails(equalTo("failureResult"))
      )
    },

    test("updateVaccination should update vaccination details for valid vaccinationId") {
      val vaccinationId = 1
      val updatedVaccinationDetails = VaccinationDetails(1, "Pfizer", "USA")
      for {
        service <- ZIO.service[VaccinationService.Service]
        vaccinations <- service.updateVaccination(vaccinationId, updatedVaccinationDetails)
        updatedVaccination <- service.getVaccinationById(vaccinationId)
      } yield assert(vaccinations.vaccinationList)(hasSameElements(service.getVaccinationList.toList)) &&
        assertTrue(updatedVaccination == updatedVaccinationDetails)
    },

    test("updateVaccination should fail with InvalidInput error for invalid vaccinationId") {
      val vaccinationId = 100
      val updatedVaccinationDetails = VaccinationDetails(1, "Pfizer", "USA")
      for {
        service <- ZIO.service[VaccinationService.Service]
        result <- service.updateVaccination(vaccinationId, updatedVaccinationDetails).exit
      } yield assert(result)(fails(equalTo(VaccinationError.InvalidInput(s"Update is failed. Vaccination Id is not available $vaccinationId"))))
    }
  ).provideLayer(vaccinationServiceLayer)

}

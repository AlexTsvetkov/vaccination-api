package com.pramod.vaccination.service

import com.pramod.vaccination.exception.VaccinationError
import com.pramod.vaccination.model.VaccinationDetails
import zio.{Scope, ZLayer}
import zio.test.*
import zio.test.{TestEnvironment, ZIOSpecAbstract, suite}
import zio.test.*
import zio.test.Assertion.*


import com.pramod.vaccination.exception.VaccinationError
import com.pramod.vaccination.model.{VaccinationDetails, Vaccinations}
import zio.test._
import zio.test.Assertion._
import zio.{ZIO, ZLayer}

object VaccinationServiceLiveTest extends ZIOSpecDefault {
  def spec = suite("VaccinationServiceLive") {

    test("updateVaccination method updates an existing vaccination with valid input") {
      val service = new VaccinationServiceLive()
      val validVaccinationId = 2
      val updatedVaccinationDetails = VaccinationDetails(2, "Moderna", "USA")
      for {
        result <- service.updateVaccination(validVaccinationId, updatedVaccinationDetails).map(_.vaccinationList)
      } yield assertTrue(result == service.VACCINATION_LIST.toList)
    }

    test("getAllVaccination should return all vaccinations") {
      val service = new VaccinationServiceLive()
      for {
        vaccinations <- service.getAllVaccination()
      } yield assert(vaccinations.vaccinationList)(hasSameElements(service.VACCINATION_LIST.toList))
    }

    test("getVaccinationById should return correct vaccination for valid vaccinationId") {
      val vaccinationId = 1
      val service = new VaccinationServiceLive()
      for {
        vaccination <- service.getVaccinationById(vaccinationId)
      } yield assert(vaccination.vaccinationId)(equalTo(vaccinationId))
    }

    /*test("getVaccinationById should fail with NotFound error for invalid vaccinationId") {
      val vaccinationId = 100
      val service = new VaccinationServiceLive()
      for {
        result <- service.getVaccinationById(vaccinationId)
      } yield assert(result)(fails(equalTo(VaccinationError.NotFound(s"Vaccination not found for $vaccinationId"))))
    }*/

    test("updateVaccination should update vaccination details for valid vaccinationId") {
      val vaccinationId = 1
      val updatedVaccinationDetails = VaccinationDetails(1, "Pfizer", "USA")
      val service = new VaccinationServiceLive()
      for {
        vaccinations <- service.updateVaccination(vaccinationId, updatedVaccinationDetails)
        updatedVaccination <- service.getVaccinationById(vaccinationId)
      } yield assert(vaccinations.vaccinationList)(hasSameElements(service.VACCINATION_LIST.toList)) &&
        assertTrue(updatedVaccination == updatedVaccinationDetails)
    }

    /*test("updateVaccination should fail with InvalidInput error for invalid vaccinationId") {
      val vaccinationId = 100
      val updatedVaccinationDetails = VaccinationDetails(1, "Pfizer", "USA")
      val service = new VaccinationServiceLive()
      for {
        result <- service.updateVaccination(vaccinationId, updatedVaccinationDetails)
      } yield assert(result)(fails(equalTo(VaccinationError.InvalidInput(s"Update is failed. Vaccination Id is not available $vaccinationId"))))
    }*/
  }

}

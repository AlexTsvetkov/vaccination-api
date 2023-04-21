package com.pramod.vaccination.service

import com.pramod.vaccination.exception.VaccinationError
import com.pramod.vaccination.model.{VaccinationDetails, Vaccinations}
import zio.{ZIO, ZLayer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait VaccinationService {
  def getAllVaccination(): ZIO[Any, Nothing, Vaccinations]

  def getVaccinationById(vaccinationId: Int): ZIO[Any, VaccinationError.NotFound, VaccinationDetails]

  def updateVaccination(vaccinationId: Int, updatedVaccinationDetails: VaccinationDetails): ZIO[Any, VaccinationError.InvalidInput, Vaccinations]

  def addVaccination(newVaccinationDetails: VaccinationDetails): ZIO[Any, VaccinationError.InvalidInput, Vaccinations]

  def deleteVaccination(vaccinationId: Int): ZIO[Any, VaccinationError.InvalidInput, Unit]
  
  def getVaccinationList: ListBuffer[VaccinationDetails]
}

object VaccinationService {
  val VACCINATION_LIST: ListBuffer[VaccinationDetails] = ListBuffer(VaccinationDetails(1, "Pfizer", "USA"),
    VaccinationDetails(2, "Moderna", "Russia"),
    VaccinationDetails(3, "Sinopharm", "China"))

  lazy val live: ZLayer[Any, Nothing, VaccinationService] = ZLayer {
    ZIO.succeed(VaccinationServiceLive())
  }
}

class VaccinationServiceLive extends VaccinationService {
  def create(vaccinationList: ListBuffer[VaccinationDetails]): VaccinationServiceLive = {
    val serviceLive = new VaccinationServiceLive()
    serviceLive.vaccinationList = vaccinationList
    serviceLive
  }

  import com.pramod.vaccination.service.VaccinationService

  var vaccinationList: ListBuffer[VaccinationDetails] = VaccinationService.VACCINATION_LIST

  override def getVaccinationList: ListBuffer[VaccinationDetails] = vaccinationList

  override def getAllVaccination(): ZIO[Any, Nothing, Vaccinations] = {
    ZIO.logInfo("Get all vaccinations") *>
      ZIO.succeed(Vaccinations(vaccinationList.toList))
  }

  override def getVaccinationById(vaccinationId: Int): ZIO[Any, VaccinationError.NotFound, VaccinationDetails] = {
    ZIO.logInfo(s"Get Vaccination for vaccinationId : $vaccinationId .") *>
      ZIO.fromOption(vaccinationDetails(vaccinationId))
        .mapError(_ => VaccinationError.NotFound(s"Vaccination not found for $vaccinationId"))
        .debug(s"Vaccination found for $vaccinationId")
  }

  override def updateVaccination(vaccinationId: Int, updatedVaccinationDetails: VaccinationDetails): ZIO[Any, VaccinationError.InvalidInput, Vaccinations] = {
    vaccinationList.find(vacDetail => vacDetail.vaccinationId.equals(vaccinationId)) match {
      case Some(vacDetails) =>
        vaccinationList.update(vaccinationList.indexOf(vacDetails), updatedVaccinationDetails)
        ZIO.logInfo(s"Update vaccination for vaccinationId : $vaccinationId") *>
          ZIO.succeed(Vaccinations(vaccinationList.toList))
      case _ =>
        ZIO.logInfo(s"Update vaccination for vaccinationId : $vaccinationId") *>
          ZIO.fail(VaccinationError.InvalidInput(s"Update is failed. Vaccination Id is not available $vaccinationId"))
    }
  }

  override def addVaccination(newVaccinationDetails: VaccinationDetails): ZIO[Any, VaccinationError.InvalidInput, Vaccinations] = {
    vaccinationList.find(vacDetail => vacDetail.vaccinationId.equals(newVaccinationDetails.vaccinationId)) match {
      case None =>
        vaccinationList += newVaccinationDetails
        ZIO.logInfo(s"Insert vaccination for vaccinationId : ${newVaccinationDetails.vaccinationId}") *>
          ZIO.succeed(Vaccinations(vaccinationList.toList))
      case Some(vacDetails) =>
        ZIO.logInfo(s"new vaccination is not added. Already exist same vaccinationId : ${vacDetails.vaccinationId}") *>
          ZIO.fail(VaccinationError.InvalidInput(s"Insert is failed. Vaccination Id is already available ${vacDetails.vaccinationId}"))
    }
  }

  override def deleteVaccination(vaccinationId: Int): ZIO[Any, VaccinationError.InvalidInput, Unit] = {
    vaccinationList.find(vacDetail => vacDetail.vaccinationId.equals(vaccinationId)) match {
      case Some(vacDetails) =>
        vaccinationList -= vacDetails
        ZIO.logInfo(s"Deleted vaccination for vaccinationId : $vaccinationId") *>
          ZIO.succeed(())
      case _ =>
        ZIO.logInfo(s"new vaccination couldn't be deleted. VaccinationId is not exist : $vaccinationId") *>
          ZIO.fail(VaccinationError.InvalidInput(s"Delete is failed. Vaccination Id is not available $vaccinationId"))
    }
  }

  private val vaccinationDetails: Int => Option[VaccinationDetails] = (vacId: Int) => vaccinationList.find(vacDetails => vacDetails.vaccinationId.equals(vacId))

}
package com.pramod.vaccination.service

import com.pramod.vaccination.exception.VaccinationError
import com.pramod.vaccination.model.{VaccinationDetails, Vaccinations}
import com.pramod.vaccination.service.VaccinationService.Service
import zio.{ZEnvironment, ZIO, ZLayer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object VaccinationService {

  trait Service {
    def getAllVaccination(): ZIO[Any, Nothing, Vaccinations]

    def getVaccinationById(vaccinationId: Int): ZIO[Any, VaccinationError.NotFound, VaccinationDetails]

    def updateVaccination(vaccinationId: Int, updatedVaccinationDetails: VaccinationDetails): ZIO[Any, VaccinationError.InvalidInput, Vaccinations]

    def addVaccination(newVaccinationDetails: VaccinationDetails): ZIO[Any, VaccinationError.InvalidInput, Vaccinations]

    def deleteVaccination(vaccinationId: Int): ZIO[Any, VaccinationError.InvalidInput, Unit]

    def getVaccinationList: ListBuffer[VaccinationDetails]
  }


  def create(repository: VaccinationRepository.Service): VaccinationService.Service =
    new VaccinationServiceLive(repository)

  lazy val live: ZLayer[VaccinationRepository.Service, Nothing, VaccinationService.Service] =
    ZLayer {
      for {
        repo: VaccinationRepository.Service <- ZIO.service[VaccinationRepository.Service]
      } yield create(repo)
    }
  
}

class VaccinationServiceLive(repository: VaccinationRepository.Service) extends VaccinationService.Service {

  import com.pramod.vaccination.service.VaccinationService

  override def getVaccinationList: ListBuffer[VaccinationDetails] = repository.getVaccinations

  override def getAllVaccination(): ZIO[Any, Nothing, Vaccinations] = {
    ZIO.logInfo("Get all vaccinations") *>
      ZIO.succeed(Vaccinations(repository.getVaccinations.toList))
  }

  override def getVaccinationById(vaccinationId: Int): ZIO[Any, VaccinationError.NotFound, VaccinationDetails] = {
    ZIO.logInfo(s"Get Vaccination for vaccinationId : $vaccinationId .") *>
      ZIO.fromOption(vaccinationDetails(vaccinationId))
        .mapError(_ => VaccinationError.NotFound(s"Vaccination not found for $vaccinationId"))
        .debug(s"Vaccination found for $vaccinationId")
  }

  override def updateVaccination(vaccinationId: Int, updatedVaccinationDetails: VaccinationDetails): ZIO[Any, VaccinationError.InvalidInput, Vaccinations] = {
    vaccinationDetails(vaccinationId) match {
      case Some(vacDetails) =>
        repository.getVaccinations.update(repository.getVaccinations.indexOf(vacDetails), updatedVaccinationDetails)
        ZIO.logInfo(s"Update vaccination for vaccinationId : $vaccinationId") *>
          ZIO.succeed(Vaccinations(repository.getVaccinations.toList))
      case _ =>
        ZIO.logInfo(s"Update vaccination for vaccinationId : $vaccinationId") *>
          ZIO.fail(VaccinationError.InvalidInput(s"Update is failed. Vaccination Id is not available $vaccinationId"))
    }
  }

  override def addVaccination(newVaccinationDetails: VaccinationDetails): ZIO[Any, VaccinationError.InvalidInput, Vaccinations] = {
    vaccinationDetails(newVaccinationDetails.vaccinationId) match {
      case None =>
        repository.getVaccinations += newVaccinationDetails
        ZIO.logInfo(s"Insert vaccination for vaccinationId : ${newVaccinationDetails.vaccinationId}") *>
          ZIO.succeed(Vaccinations(repository.getVaccinations.toList))
      case Some(vacDetails) =>
        ZIO.logInfo(s"new vaccination is not added. Already exist same vaccinationId : ${vacDetails.vaccinationId}") *>
          ZIO.fail(VaccinationError.InvalidInput(s"Insert is failed. Vaccination Id is already available ${vacDetails.vaccinationId}"))
    }
  }

  override def deleteVaccination(vaccinationId: Int): ZIO[Any, VaccinationError.InvalidInput, Unit] = {
    vaccinationDetails(vaccinationId) match {
      case Some(vacDetails) =>
        repository.getVaccinations -= vacDetails
        ZIO.logInfo(s"Deleted vaccination for vaccinationId : $vaccinationId") *>
          ZIO.succeed(())
      case _ =>
        ZIO.logInfo(s"new vaccination couldn't be deleted. VaccinationId is not exist : $vaccinationId") *>
          ZIO.fail(VaccinationError.InvalidInput(s"Delete is failed. Vaccination Id is not available $vaccinationId"))
    }
  }

  private val vaccinationDetails: Int => Option[VaccinationDetails] = (vacId: Int) => repository.getVaccinations.find(vacDetails => vacDetails.vaccinationId.equals(vacId))

}
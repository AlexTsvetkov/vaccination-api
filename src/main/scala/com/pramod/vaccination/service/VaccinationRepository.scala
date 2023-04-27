package com.pramod.vaccination.service

import com.pramod.vaccination.exception.VaccinationError
import com.pramod.vaccination.model.{VaccinationDetails, Vaccinations}
import com.pramod.vaccination.service.VaccinationService.Service
import zio.{ZIO, ZLayer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object VaccinationRepository {

  trait Service {
    def getVaccinations: ListBuffer[VaccinationDetails]
  }

  val VACCINATION_LIST: ListBuffer[VaccinationDetails] = ListBuffer(VaccinationDetails(1, "Pfizer", "USA"),
    VaccinationDetails(2, "Moderna", "Russia"),
    VaccinationDetails(3, "Sinopharm", "China"))

  def create: VaccinationRepository.Service = new VaccinationRepositoryLive
  
  lazy val live: ZLayer[Any, Nothing, VaccinationRepository.Service] = ZLayer {
    ZIO.succeed(create)
  }
}

class VaccinationRepositoryLive extends VaccinationRepository.Service {

  import com.pramod.vaccination.service.VaccinationRepository

  var vaccinationList: ListBuffer[VaccinationDetails] = VaccinationRepository.VACCINATION_LIST

  override def getVaccinations: ListBuffer[VaccinationDetails] = vaccinationList

}
import doobie.implicits._
import cats.effect.unsafe.implicits.global

import cats.effect.{IO, IOApp}
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._

import java.io.File
import models.Estudiantes
import dao.EstudiantesDAO
import config.Database

// Extiende de IOApp.Simple para manejar efectos IO y recursos de forma segura
object Main extends IOApp.Simple {
  val (transactor, cleanup) = Database.transactor.allocated.unsafeRunSync()

  // hacemos la lectura del csv
  val path2DataFile2 = "src/main/resources/data/estudiantes.csv"

  //aqui se crea el nuevo archivo
  val dataSource = new File(path2DataFile2)
    .readCsv[List, Estudiantes](rfc.withHeader.withCellSeparator(','))

  // aqui seleccionamos desde el dataSource los datos de estudiantes
  val estudiantes = dataSource.collect {
    case Right(estudiante) => estudiante
  }

  // Secuencia de operaciones IO usando for-comprehension
  def run: IO[Unit] = for {
    result <- EstudiantesDAO.insertAll(estudiantes) // Inserta datos y extrae resultado con <-
    _ <- IO.println(s"Registros insertados: ${result.size}")  // Imprime cantidad
  } yield ()  // Completa la operación

  // aqui asemos una variable estudiantesSQL en el cual tenemos el llamado a la funcion de obtener todos los datos y despues los presentamos en pantalla
  val estudiantesSQL = EstudiantesDAO.obtenerTodos.transact(transactor).unsafeRunSync()
  println(s"Estudiantes: $estudiantesSQL")

}
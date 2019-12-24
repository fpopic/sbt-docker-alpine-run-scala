package com.github.fpopic

import java.io.File

import com.sun.security.auth.module.UnixSystem
import pureconfig.ConfigConvert.catchReadError
import pureconfig.ConfigReader
import pureconfig.ConfigSource.default._
import pureconfig.generic.auto._
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {

  /** PureConfig [[ConfigReader]] for reading config strings as [[AnyRef]] */
  implicit val anyRefConvert: ConfigReader[AnyRef] =
    ConfigReader.fromString[AnyRef](catchReadError(_.asInstanceOf[AnyRef]))

  def main(args: Array[String]): Unit = {
    val us = new UnixSystem()
    logger.info(s"Username:${us.getUsername}, GID:${us.getGid}, UID:${us.getUid}.")

    val wd = new File("/app/")
    logger.info(s"Files in:${wd.getAbsolutePath}")
    wd.listFiles.foreach(f => logger.info(f.getName))

    val config = loadOrThrow[MyConfig]
    logger.info(s"Starting pipeline with configuration: $config")
  }

  private case class MyConfig(x: String)

}

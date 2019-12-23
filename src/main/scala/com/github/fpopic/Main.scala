package com.github.fpopic

import java.io.File

import com.sun.security.auth.module.UnixSystem

object Main {

  def main(args: Array[String]): Unit = {
    val us = new UnixSystem()
    println(s"ENV: ${System.getenv()}.")
    println(s"Username:${us.getUsername}, GID:${us.getGid}, UID:${us.getUid}.")

    val wd = new File(".")
    println(s"Files in:${wd.getAbsolutePath}")
    wd.listFiles().foreach(println)
  }

}

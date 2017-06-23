package amf.common

import java.io.{File, FileNotFoundException, FileReader, Reader}
import java.net.{InetAddress, UnknownHostException}

import org.scalatest.Matchers._

import scala.util.Random

/**
  * Created by martin.gutierrez on 6/21/17.
  */
object Tests {

  /** Assert against a specified golden File. */
  def assertEquals(outFile: File, goldenFile: File): Boolean = {
    try {
      Diff.trimming.diff(new FileReader(outFile), new FileReader(goldenFile)).isEmpty
    } catch {
      case _: FileNotFoundException => false
    }
  }

  /** Assert that the field is not null and return it. */
  def assertNotNull[T](e: T): T = {
    e should not equal null
    e
  }

  /** Check against a specified golden File and fail. */
  def checkDiff(outFile: File, goldenFile: File): Unit = {
    val difference: Option[String] = diff(outFile, goldenFile)

    println("IS PRESENT!!!!!! : " + difference.isDefined)
    difference.map(s => fail(s))
  }

  /** Check against a specified golden File. */
  def checkDiff(a: File, b: Reader): Unit = {
    try {
      checkDiff(new FileReader(a), a.toString, b, "")
    } catch {
      case _: FileNotFoundException => fail("Cannot Open File: " + a)
    }
  }

  /** Diff between 2 strings. */
  def checkDiff(val1: String, val2: String): Unit = {
    val diffs: List[Diff.Delta[String]] = Diff.trimming.diff(val1, val2)
    if (diffs.nonEmpty) fail("\n" + Diff.makeString(diffs))
  }

  /** Check against a specified golden File. */
  def checkDiff(outFile: File, goldenFile: File, comparator: Diff.Equals[String]): Unit = {
    val diffs: List[Diff.Delta[String]] = Diff.stringDiffer(comparator).diff(outFile, goldenFile)
    if (diffs.nonEmpty) fail("\ndiff -y -W 150 " + outFile + " " + goldenFile + "\n" + Diff.makeString(diffs))
  }

  /** Return optional diff with specified golden file. */
  def diff(outFile: File, goldenFile: File): Option[String] = {
    if (!goldenFile.exists())
      fail(
        String.format("Cannot Open Golden File: \n%s\nfor comparing \n%s",
                      goldenFile.getAbsolutePath,
                      outFile.getAbsolutePath))
    try {
      diff(new FileReader(outFile), outFile.toString, new FileReader(goldenFile), goldenFile.toString)
    } catch {
      case e: FileNotFoundException => fail("Cannot Open Target or Golden File: " + e.getMessage)
    }
    Option.empty
  }

  /** Get the files in the specified directory as parameters. */
  def listFiles(dirName: String, pattern: String): List[Array[_]] = {
    listFiles(new File(dirName), pattern)
  }

  /** Get the files in the specified directory as parameters. */
  def listFiles(dir: File, pattern: String): List[Array[_]] = {
    Files.list(dir, pattern).map(path => Array(new File(path)))
  }

  /** Return a random string with the specified length. */
  def randomString(length: Int): String = {
    val r: Random             = new Random()
    val result: StringBuilder = new StringBuilder()
    (0 until length).foreach(result.append(('a' + r.nextInt('z' - 'a')).toChar))
    result.toString
  }

  /** Sleep during the specified number of milliseconds. */
  def sleep(millis: Int): Unit = {
    System.out.printf("Sleeping %s ms....", millis.toString)
    Thread.sleep(millis)

    System.out.println("\r ")
  }

  /** Wrap single parameter as a List of objects (To be used by @Params in junit. */
  def wrapForParameters(args: Array[_]): List[Array[_]] = {
    args.map(arg => Array(arg)).toList
  }

  /** Returns the hostname. */
  def getHostName: String = {
    try {
      val host: String = InetAddress.getLocalHost.getHostName.replaceAll("-", "")
      host.substring(0, Math.min(index(host, '.'), Math.min(index(host, '_'), index(host, '-'))));
    } catch {
      case e: UnknownHostException => "localhost";
    }
  }

  def checkDiff(a: Reader, aName: String, b: Reader, bName: String): Unit = {
    val diffs: List[Diff.Delta[String]] = Diff.ignoreAllSpace.diff(a, b)
    if (diffs.nonEmpty) fail("\ndiff -y -W 150 " + aName + " " + bName + "\n" + Diff.makeString(diffs))
  }

  def diff(a: Reader, aName: String, b: Reader, bName: String): Option[String] = {
    val diffs: List[Diff.Delta[String]] = Diff.ignoreAllSpace.diff(a, b)
    if (diffs.isEmpty) Option.empty
    else Option("\ndiff -y -W 150 " + aName + " " + bName + "\n" + Diff.makeString(diffs))
  }

  private def index(s: String, chr: Char): Int = {
    val n: Int = s.indexOf(chr)
    if (n <= 0) s.length() else n
  }

  //~ Inner Classes ................................................................................................................................

  private class GoldenTest(val outputFile: File, val goldenFile: File) {

    /** Check the output against the golden file. */
    def check(): Unit = {
      checkDiff(outputFile, goldenFile)
    }
  }

  private object GoldenTest {
    def apply(testFile: File, outputDirName: String): GoldenTest = {
      val name: String     = testFile.getName
      val outputFile: File = createOutputFile(outputDirName, name)
      val dot: Int         = name.lastIndexOf('.')
      val bareName: String = if (dot == -1) name else name.substring(0, dot)
      val goldenFile: File = new File(testFile.getParent, bareName + ".golden")
      new GoldenTest(outputFile, goldenFile)
    }

    def apply(testName: String, outputDirName: String, goldenDirName: String): GoldenTest = {
      new GoldenTest(createOutputFile(outputDirName, testName), new File(goldenDirName, testName))
    }

    def createOutputFile(outputDirName: String, name: String): File = {
      val outputDir: File = new File(outputDirName)
      outputDir.mkdirs()
      new File(outputDir, name)
    }
  }
}

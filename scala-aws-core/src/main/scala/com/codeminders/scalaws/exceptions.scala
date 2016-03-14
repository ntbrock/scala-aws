package com.codeminders.scalaws

import scala.xml._

trait AmazonClientException extends Exception with Serializable {
}

object AmazonClientException extends Serializable {
  def apply(message: String, t: Throwable) = new Exception(message, t) with AmazonClientException
  def apply(message: String) = new Exception(message) with AmazonClientException
  def apply(t: Throwable) = new Exception(t) with AmazonClientException
}

class AmazonServiceException(val statusCode: Int, val xml: Seq[Node]) extends Exception("%d: %s".format(statusCode, xml.toString)) with Serializable {
  lazy val errorCode = xml \ "Code" text
  lazy val message = xml \ "Message" text
  lazy val resource = xml \ "Resource" text
  lazy val requestId = xml \ "RequestId" text
}

class NoSuchBucketException(xml: Seq[Node]) extends AmazonServiceException(404, xml) with Serializable {
  lazy val bucketName = xml \ "BucketName" text
  lazy val hostId = xml \ "HostId" text
}

class NoSuchKeyException(xml: Seq[Node]) extends AmazonServiceException(404, xml) with Serializable {
  lazy val key = xml \ "Key" text
}

object AmazonServiceException extends Serializable {
  def apply(statusCode: Int, xml: Seq[Node]): AmazonServiceException = {
    xml \ "Code" head match {
      case <Code>NoSuchBucket</Code> => new NoSuchBucketException(xml)
      case <Code>NoSuchKey</Code> => new NoSuchKeyException(xml)
      case _ => new AmazonServiceException(statusCode, xml)
    }
  }

  def apply(statusCode: Int): AmazonServiceException = {
    new AmazonServiceException(statusCode,
      <Error>
        <Code/>
        <Message/>
        <Resource/>
        <RequestId/>
      </Error>)
  }
}

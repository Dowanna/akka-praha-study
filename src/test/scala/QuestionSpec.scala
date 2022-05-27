package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import scala.concurrent.duration.FiniteDuration
import org.scalatest.wordspec.AnyWordSpecLike

class QuestionActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  import adaptor.actor.QuestionActor._

  "Device Query Group" must {
    "devices in map receive a ReadTemperature message" in {
      val testProbe = createTestProbe[RespondAllTemperatures]()
      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()
      val mapIdToDeviceActor = Map("device1" -> device1.ref, "device2" -> device2.ref)
      val queryActor =
        spawn(DeviceGroupQuery(mapIdToDeviceActor, requestId = 1, requester = testProbe.ref, timeout = FiniteDuration(3, "seconds")))

      device1.expectMessageType[ReadTemperature]
      device2.expectMessageType[ReadTemperature]
    }
    "return timeout message when device does not respond" in {
      val testProbe = createTestProbe[RespondAllTemperatures]()
      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()
      val mapIdToDeviceActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(mapIdToDeviceActor, requestId = 1, requester = testProbe.ref, timeout = FiniteDuration(200, "millis")))
      queryActor ! DeviceGroupQuery.WrappedRespondTemperature(Device.RespondTemperature(requestId = 1, "device1", Some(1.0)))

      testProbe.expectMessage(RespondAllTemperatures(requestId = 1, Map("device1" -> Temperature(1.0), "device2" -> DeviceTimeOut)))
    }
    "return temperature value for working devices" in {
      val testProbe = createTestProbe[RespondAllTemperatures]()
      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()
      val mapIdToDeviceActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(mapIdToDeviceActor, requestId = 1, requester = testProbe.ref, timeout = FiniteDuration(3, "seconds")))
      queryActor ! DeviceGroupQuery.WrappedRespondTemperature(Device.RespondTemperature(requestId = 1, "device1", Some(1.0)))
      queryActor ! DeviceGroupQuery.WrappedRespondTemperature(Device.RespondTemperature(requestId = 1, "device2", Some(2.0)))

      testProbe.expectMessage(RespondAllTemperatures(requestId = 1, Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0))))
    }
    "return temperature not avilable for devices with no readings" in {
      val testProbe = createTestProbe[RespondAllTemperatures]()
      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()
      val mapIdToDeviceActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(mapIdToDeviceActor, requestId = 1, requester = testProbe.ref, timeout = FiniteDuration(3, "seconds")))
      queryActor ! DeviceGroupQuery.WrappedRespondTemperature(response = Device.RespondTemperature(requestId = 1, "device1", None))
      queryActor ! DeviceGroupQuery.WrappedRespondTemperature(response = Device.RespondTemperature(requestId = 1, "device2", Some(2.0)))

      testProbe.expectMessage(
        RespondAllTemperatures(requestId = 1, temperatures = Map("device1" -> TemperatureNotAvailable, "device2" -> Temperature(2.0)))
      )
    }
    "return deviceNotAvailable if device stops before answering" in {
      val testProbe = createTestProbe[RespondAllTemperatures]()
      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()
      val mapIdToDeviceActor = Map("device1" -> device1.ref, "device2" -> device2.ref)
      val queryActor =
        spawn(DeviceGroupQuery(mapIdToDeviceActor, requestId = 1, requester = testProbe.ref, timeout = FiniteDuration(3, "seconds")))

      queryActor ! DeviceGroupQuery.WrappedRespondTemperature(response = Device.RespondTemperature(requestId = 1, "device1", Some(1.0)))
      device2.stop()

      testProbe.expectMessage(RespondAllTemperatures(1, Map("device1" -> Temperature(1.0), "device2" -> DeviceNotAvaialble)))
    }
  }
  "Device Actor" must {
    "reply with empty reading initially" in {
      val probe = createTestProbe[RespondTemperature]()
      val deviceActor = spawn(Device("group", "device"))

      deviceActor ! ReadTemperature(requestId = 42, probe.ref)

      val response = probe.receiveMessage()
      response.requestId should ===(42)
      response.value should ===(None)
    }
    "reply with latest temperature reading" in {
      val readProbe = createTestProbe[RespondTemperature]() // 気温を問い合わせるアクター
      val writeProbe = createTestProbe[RecordTemperatureComplete]() // 気温の書き込みを依頼するアクター
      val deviceActor = spawn(Device("group", "device"))

      deviceActor ! RecordTemperature(requestId = 1, value = 24.0, replyTo = writeProbe.ref)
      writeProbe.expectMessage(RecordTemperatureComplete(requestId = 1))

      deviceActor ! ReadTemperature(requestId = 2, readProbe.ref)
      readProbe.expectMessage(RespondTemperature(requestId = 2, deviceId = "device", value = Some(24.0)))
    }
  }
  "Device Group" must {
    "be able to return temperature reading of all registered devices" in {
      val registerTrackProbe = createTestProbe[DeviceRegistered]()
      val registerTemperatureProbe = createTestProbe[RecordTemperatureComplete]()
      val groupActor = spawn(DeviceGroup("group"))
      groupActor ! RequestTrackDevice("group", "device1", registerTrackProbe.ref)
      val device1 = registerTrackProbe.receiveMessage().device
      groupActor ! RequestTrackDevice("group", "device2", registerTrackProbe.ref)
      val device2 = registerTrackProbe.receiveMessage().device
      groupActor ! RequestTrackDevice("group", "device3", registerTrackProbe.ref)
      registerTrackProbe.receiveMessage()
      device1 ! RecordTemperature(requestId = 0, value = 1.0, registerTemperatureProbe.ref)
      registerTemperatureProbe.expectMessage(RecordTemperatureComplete(requestId = 0))
      device2 ! RecordTemperature(requestId = 1, value = 2.0, registerTemperatureProbe.ref)
      registerTemperatureProbe.expectMessage(RecordTemperatureComplete(requestId = 1))

      val requestAllTeperaturesProbe = createTestProbe[RespondAllTemperatures]()
      groupActor.ref.tell(RequestAllTemperatures(999, "group", requestAllTeperaturesProbe.ref)) // tellでも!でもok

      requestAllTeperaturesProbe.expectMessage(
        RespondAllTemperatures(
          999,
          Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0), "device3" -> TemperatureNotAvailable)
        )
      )
    }
    "be able to list active devices after one shuts down" in {
      val registeredProbe = createTestProbe[DeviceRegistered]()
      val listResultProbe = createTestProbe[DeviceList]()
      val group = spawn(DeviceGroup("group"))
      group ! RequestTrackDevice("group", "device1", registeredProbe.ref)
      val deviceToTerminate = registeredProbe.receiveMessage().device
      group ! RequestTrackDevice("group", "device2", registeredProbe.ref)
      deviceToTerminate ! Passivate
      registeredProbe.expectTerminated(deviceToTerminate, registeredProbe.remainingOrDefault)

      registeredProbe.awaitAssert({
        group ! RequestDeviceList("group", listResultProbe.ref)
        listResultProbe.expectMessage(DeviceList("group", Set("device2")))
      })
    }
    "be able to return list of all devices" in {
      val someProbe = createTestProbe[DeviceRegistered]()
      val probe = createTestProbe[DeviceList]()
      val deviceGroup = spawn(DeviceGroup("group"))
      deviceGroup ! RequestTrackDevice("group", "device1", someProbe.ref)
      deviceGroup ! RequestTrackDevice("group", "device2", someProbe.ref)
      deviceGroup ! RequestDeviceList("group", probe.ref)

      val received = probe.receiveMessage()
      println("received devices: ")
      println(received)
      received.devices.shouldEqual(Set("device1", "device2"))

      // 関係ないグループidの時は何もメッセージを返さない
      deviceGroup ! RequestDeviceList("group-unrelated", probe.ref)
      probe.expectNoMessage(FiniteDuration(500, "millis"))
    }
    "be able to register a device actor" in {
      val probe = createTestProbe[DeviceRegistered]()
      val deviceGroupActorRef = spawn(DeviceGroup("group"))

      // can register multiple devices
      deviceGroupActorRef ! RequestTrackDevice("group", "device1", probe.ref)
      val registered1 = probe.receiveMessage()
      val device1 = registered1.device
      deviceGroupActorRef ! RequestTrackDevice("group", "device2", probe.ref)
      val registered2 = probe.receiveMessage()
      val device2 = registered2.device
      device1 should !==(device2)

      // check that actors are working
      val recordProbe = createTestProbe[RecordTemperatureComplete]()
      device1 ! RecordTemperature(requestId = 0, value = 1.0, recordProbe.ref)
      recordProbe.expectMessage((RecordTemperatureComplete(requestId = 0)))
      device2 ! RecordTemperature(requestId = 1, value = 3.0, recordProbe.ref)
      recordProbe.expectMessage((RecordTemperatureComplete(requestId = 1)))
    }
    "return same actor for same deviceId" in {
      val probe = createTestProbe[DeviceRegistered]()
      val deviceGroup = spawn(DeviceGroup("groupId"))
      deviceGroup ! RequestTrackDevice("groupId", "deviceId", probe.ref)
      val registered1 = probe.receiveMessage()
      val device1 = registered1.device

      deviceGroup ! RequestTrackDevice("groupId", "deviceId", probe.ref)
      val registered2 = probe.receiveMessage()
      val device2 = registered2.device

      device1 should ===(device2)
    }
    "ignore requests for wrong groupid" in {
      val probe = createTestProbe[DeviceRegistered]()
      val deviceGroup = spawn(DeviceGroup("groupId"))

      deviceGroup ! RequestTrackDevice("wrongGroupId", "deviceId", probe.ref)
      probe.expectNoMessage(FiniteDuration(500, "millis"))
    }
  }
}

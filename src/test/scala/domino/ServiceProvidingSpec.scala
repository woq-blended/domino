package domino

import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import org.osgi.framework.ServiceRegistration
import domino.test.PojoSrTestHelper
import domino.test.PojoSrTestHelper
import org.osgi.framework.ServiceListener

object ServiceProvidingSpec {

  trait MyService {
    def doIt()
  }

  trait MyService2

  class CombinedService extends MyService with MyService2 {
    def doIt() {}
  }

}

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class ServiceProvidingSpec
    extends WordSpecLike
    with ShouldMatchers
    with PojoSrTestHelper {

  import ServiceProvidingSpec._

  val exampleService = new MyService with MyService2 {
    def doIt() {}
  }

  val combinedService = new CombinedService

  val serviceProps = Map("prop1" -> "value1", "prop2" -> 3)

  "Service providing" should {

    "allow specifying just one interface" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            combinedService.providesService[CombinedService]
          }
        }
        activator.start(sr.getBundleContext)
        val ref = sr.getServiceReference(classOf[CombinedService].getName)
        assert(ref !== null)
        assert(sr.getService(ref).isInstanceOf[CombinedService])
        assert(sr.getServiceReference(classOf[MyService].getName) === null)
        assert(sr.getServiceReference(classOf[MyService2].getName) === null)
      }
    }

    "allow specifying just one interface and passing service properties" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService](
            "prop1" -> "value1",
            "prop2" -> 3
          )
        }
      }
      pending
    }

    "allow specifying just one interface and passing service properties in a map" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService](serviceProps)
        }
      }
      pending
    }

    "allow specifying several interfaces" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2]
          }
        }
        activator.start(sr.getBundleContext)
        val ref1 = sr.getServiceReference(classOf[MyService].getName)
        assert(ref1 !== null)
        val ref2 = sr.getServiceReference(classOf[MyService2].getName)
        assert(ref2 !== null)
        assert(sr.getService(ref1) === sr.getService(ref2))
      }
    }

    "allow specifying several interfaces and passing service properties" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2](
            "prop1" -> "value1",
            "prop2" -> 3
          )
        }
      }
      pending
    }

    "allow specifying several interfaces and passing service properties in a map" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2](serviceProps)
        }
      }
      pending
    }

    "allow specifying generic types" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = List(exampleService).providesService[List[MyService]]
        }
      }
      pending
    }
  }
}
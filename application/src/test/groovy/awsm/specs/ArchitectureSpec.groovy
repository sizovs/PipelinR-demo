package awsm.specs

import com.tngtech.archunit.core.importer.ClassFileImporter
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noCodeUnits
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

class ArchitectureSpec extends Specification {

  final allClasses() {
    new ClassFileImporter().importPackages("awsm")
  }

  def "all test classes follow naming convention"() {
    given:
      final rule = classes()
              .that()
              .areAssignableTo(Specification.class)
              .should()
              .haveSimpleNameEndingWith("Spec")
    expect:
      rule.check(allClasses())
  }

  def "all dependencies to jOOQ are in repositories or in a special infra package"() {
    given:
      final rule = noClasses()
              .that()
              .doNotHaveSimpleName("Repository")
              .and()
              .resideOutsideOfPackage("awsm.infrastructure.jooq..")
              .should()
              .dependOnClassesThat()
              .resideInAnyPackage("..jooq..")
    expect:
      rule.check(allClasses())
  }

  def "no package cycles allowed"() {
    given:
      final rule = slices().matching("..(*)..").should().beFreeOfCycles()
    expect:
      rule.check(allClasses())
  }

  def "no @Autowired annotation allowed"() {
    given:
      final rule = noCodeUnits()
              .should()
              .beAnnotatedWith(Autowired)
              .because("Spring uses constructor injection by default and the annotation is redundand")
    expect:
      rule.check(allClasses())
  }

}

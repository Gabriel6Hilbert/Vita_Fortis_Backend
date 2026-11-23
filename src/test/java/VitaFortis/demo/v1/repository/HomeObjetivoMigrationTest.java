package VitaFortis.demo.v1.repository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
class HomeObjetivoMigrationTest {
 @Test void criaQuatroObjetivosSemAlterarFiltros(){String url="jdbc:h2:mem:home_objetivos;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";try(var c=DriverManager.getConnection(url,"sa","");var s=c.createStatement()){s.execute("create table marcador(id int primary key)");}catch(Exception e){throw new RuntimeException(e);}Flyway f=Flyway.configure().dataSource(url,"sa","").baselineOnMigrate(true).baselineVersion("6").target("7").load();assertEquals(1,f.migrate().migrationsExecuted);try(var c=DriverManager.getConnection(url,"sa","");var r=c.createStatement().executeQuery("select count(*),count(distinct chave),count(distinct ordem) from home_objetivo")){r.next();assertEquals(4,r.getInt(1));assertEquals(4,r.getInt(2));assertEquals(4,r.getInt(3));}catch(Exception e){throw new RuntimeException(e);}}
}

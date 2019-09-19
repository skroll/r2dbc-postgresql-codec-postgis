package com.github.skroll.r2dbc.postgresql.codec.postgis;

import com.github.skroll.r2dbc.postgresql.codec.postgis.util.PostgresqlServerExtension;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;

import io.r2dbc.postgresql.PostgresqlResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PostGisCodecIntegrationTest {
  @RegisterExtension
  static final PostgresqlServerExtension SERVER = new PostgresqlServerExtension();

  private final PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
      .database(SERVER.getDatabase())
      .host(SERVER.getHost())
      .port(SERVER.getPort())
      .password(SERVER.getPassword())
      .username(SERVER.getUsername())
      .build());

  @Test
  void shouldRegisterCodec() {
    PostgresqlConnection connection = this.connectionFactory.create().block();

    connection.createStatement("DROP TABLE IF EXISTS codec_geometry_test;CREATE TABLE codec_geometry_test (my_value geometry(POINT,4326));")
        .execute().flatMap(PostgresqlResult::getRowsUpdated).then()
        .as(StepVerifier::create).verifyComplete();

    connection.createStatement("INSERT INTO codec_geometry_test VALUES(ST_GeomFromText('POINT(-73.985744 40.748549)',4326));")
        .execute().flatMap(PostgresqlResult::getRowsUpdated).then()
        .as(StepVerifier::create).verifyComplete();

    connection.createStatement("SELECT * FROM codec_geometry_test")
        .execute()
        .flatMap(it -> it.map((row, rowMetadata) -> row.get(0)))
        .cast(Geometry.class)
        .as(StepVerifier::create)
        .consumeNextWith(geometry -> {
          assertThat(geometry.getDimension()).isEqualTo(2);
          assertThat(geometry.getType()).isEqualTo(Geometry.POINT);
          assertThat(geometry).isInstanceOf(Point.class);
          final Point point = (Point) geometry;
          assertThat(point.getX()).isCloseTo(-73.985744, within(0.0000001));
          assertThat(point.getY()).isCloseTo(40.748549, within(0.0000001));
        })
        .verifyComplete();
  }
}

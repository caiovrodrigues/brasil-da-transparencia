package com.br.transparencia.etl.tjdft.job;

import com.br.transparencia.etl.tjdft.PersonTjdft;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class TjdftBatchConfig {

    @Bean
    public FlatFileItemReader<PersonTjdft> reader(){
        return new FlatFileItemReaderBuilder<PersonTjdft>()
                .name("personTjdftItemReader")
                .resource(new ClassPathResource("data/tjdft/tjdft-202503.csv"))
                .delimited()
                .names(new String[] {"nome", "cargoFuncao", "lotacao", "rendimentoTotal", "descontos", "rendimentoLiquido"})
                .fieldSetMapper(fieldSet -> {
                    PersonTjdft personTjdft = new PersonTjdft();
                    personTjdft.setNome(fieldSet.readString(0));
                    personTjdft.setCargoFuncao(fieldSet.readString(1));
                    personTjdft.setLotacao(fieldSet.readString(2));
                    personTjdft.setRendimentoTotal(BigDecimal.valueOf(Double.parseDouble(fieldSet.readString(3).replace(",", "."))));
                    personTjdft.setDescontos(BigDecimal.valueOf(Double.parseDouble(fieldSet.readString(4).replace(",", "."))));
                    personTjdft.setRendimentoLiquido(BigDecimal.valueOf(Double.parseDouble(fieldSet.readString(5).replace(",", "."))));
                    personTjdft.setReferenceDate(LocalDate.of(2025, 3, 5));
                    return personTjdft;
                })
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<PersonTjdft> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PersonTjdft>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO person_tjdft (nome, cargo_funcao, lotacao, rendimento_total, descontos, rendimento_liquido, reference_date) VALUES (:nome, :cargoFuncao, :lotacao, :rendimentoTotal, :descontos, :rendimentoLiquido, :referenceDate)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<PersonTjdft> reader, JdbcBatchItemWriter<PersonTjdft> writer) {
        return new StepBuilder("tjdftStep", jobRepository)
                .<PersonTjdft, PersonTjdft>chunk(10, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("importUserJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public CommandLineRunner runJob(JobLauncher jobLauncher, Job job) {
        return args -> {
            try{
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();
                jobLauncher.run(job, jobParameters);
                System.out.println("Batch job has been invoked!");
            }catch (Exception e){
                System.out.println("Job failed: " + e.getMessage());
            }
        };
    }

}

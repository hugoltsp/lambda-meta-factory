package com.teles.lambda.meta.factory;

public class App {

    public static void main(String[] args) {

        Person person = new Person();
        person.setName("Pablo");
        person.setLastName("Vitar");

        Car car = new Car();
        car.setModel("Del Rey GLX");
        car.setManufacturer("Ford");

        System.out.println(new LambdaMetaFactoryAccessor(Person.class).toCsvString(person));

        System.out.println(new LambdaMetaFactoryAccessor(Car.class).toCsvString(car));

    }

}

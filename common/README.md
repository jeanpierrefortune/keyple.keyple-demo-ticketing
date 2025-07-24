# Keyple Demo Common Library

This repository contains the common elements of the Keyple demo applications (data model, Intercode parser/generator, etc...).

The demo applications are an open source project provided by [Calypso Networks Association](https://calypsonet.org).

## Keyple Demos

This library is used by the following demos:
* [Keyple Reload Demo](https://github.com/calypsonet/keyple-demo-ticketing-reloading-remote)
* [Keyple Validation Demo](https://github.com/calypsonet/keyple-demo-ticketing-validation-app)
* [Keyple Control Demo](https://github.com/calypsonet/keyple-demo-ticketing-control-app)

## Calypso Card Applications

The demos works with the cards provided in the [Test kit](https://calypsonet.org/technical-support-documentation/)

This demo can be used with Calypso cards with the following card application identifiers:
* AID A000000291FF9101 - Keyple Generic test card
* AID 315449432E49434131 - CD Light/GTML Compatibility
* AID 315449432E49434133 - Calypso Light
* AID A0000004040125090101 - Navigo IDF

## Data Structures

### Environment/Holder structure

| Field Name           | Bits | Description                                        |     Type      |  Status   |
|:---------------------|-----:|:---------------------------------------------------|:-------------:|:---------:|
| EnvVersionNumber     |    8 | Data structure version number                      | VersionNumber | Mandatory | 
| EnvApplicationNumber |   32 | Card application number (unique system identifier) |      Int      | Mandatory |
| EnvIssuingDate       |   16 | Card application issuing date                      |  DateCompact  | Mandatory | 
| EnvEndDate           |   16 | Card application expiration date                   |  DateCompact  | Mandatory | 
| HolderCompany        |    8 | Holder company                                     |      Int      | Optional  | 
| HolderIdNumber       |   32 | Holder Identifier within HolderCompany             |      Int      | Optional  | 
| EnvPadding           |  120 | Padding (bits to 0)                                |    Binary     | Optional  | 

### Event structure

| Field Name         | Bits | Description                                   |     Type      |  Status   |
|:-------------------|-----:|:----------------------------------------------|:-------------:|:---------:|
| EventVersionNumber |    8 | Data structure version number                 | VersionNumber | Mandatory | 
| EventDateStamp     |   16 | Date of the event                             |  DateCompact  | Mandatory | 
| EventTimeStamp     |   16 | Time of the event                             |  TimeCompact  | Mandatory | 
| EventLocation      |   32 | Location identifier                           |      Int      | Mandatory | 
| EventContractUsed  |    8 | Index of the contract used for the validation |      Int      | Mandatory | 
| ContractPriority1  |    8 | Priority for contract #1                      | PriorityCode  | Mandatory | 
| ContractPriority2  |    8 | Priority for contract #2                      | PriorityCode  | Mandatory | 
| ContractPriority3  |    8 | Priority for contract #3                      | PriorityCode  | Mandatory | 
| ContractPriority4  |    8 | Priority for contract #4                      | PriorityCode  | Mandatory | 
| EventPadding       |  120 | Padding (bits to 0)                           |    Binary     | Optional  | 

### Contract structure

| Field Name              | Bits | Description                          |        Type         |  Status   |
|:------------------------|-----:|:-------------------------------------|:-------------------:|:---------:|
| ContractVersionNumber   |    8 | Data structure version number        |    VersionNumber    | Mandatory | 
| ContractTariff          |    8 | Contract Type                        |    PriorityCode     | Mandatory | 
| ContractSaleDate        |   16 | Sale date of the contract            |     DateCompact     | Mandatory | 
| ContractValidityEndDate |   16 | Last day of validity of the contract |     DateCompact     | Mandatory | 
| ContractSaleSam         |   32 | SAM which loaded the contract        |         Int         | Optional  | 
| ContractSaleCounter     |   24 | SAM auth key counter value           |         Int         | Optional  | 
| ContractAuthKvc         |    8 | SAM auth key KVC                     |         Int         | Optional  | 
| ContractAuthenticator   |   24 | Security authenticator               | Authenticator (Int) | Optional  | 
| ContractPadding         |   96 | Padding (bits to 0)                  |       Binary        | Optional  | 

### Counter structure

| Field Name   | Bits | Description     | Type |  Status   |
|:-------------|-----:|:----------------|:----:|:---------:|
| CounterValue |   24 | Number of trips | Int  | Mandatory | 

## Data Types

| Name          | Bits | Description                                                                                                                                                        |
|:--------------|-----:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DateCompact   |   16 | Number of days since January 1st, 2010 (being date 0). Maximum value is 16,383, last complete year being 2053. All dates are in legal local time.                  |
| PriorityCode  |    8 | Types of contracts defined: <br>0 Forbidden (present in clean records only)<br>1 Season Pass<br>2 Multi-trip ticket<br>3 Stored Value<br>4 to 30 RFU<br>31 Expired |
| TimeCompact   |   16 | Time in minutes, value = hour*60+minute (0 to 1,439)                                                                                                               |    
| VersionNumber |    8 | Data model version:<br>0 Forbidden (undefined)<br>1 Current version<br>2..254 RFU<br>255 Forbidden (reserved)                                                      |


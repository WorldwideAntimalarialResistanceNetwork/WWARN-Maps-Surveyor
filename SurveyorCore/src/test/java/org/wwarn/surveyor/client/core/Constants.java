package org.wwarn.surveyor.client.core;

/*
 * #%L
 * SurveyorCore
 * %%
 * Copyright (C) 2013 - 2014 University of Oxford
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the University of Oxford nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

/**
 * Created with IntelliJ IDEA.
 * User: nigel
 * Date: 26/07/13
 * Time: 10:40
 */
public interface Constants {
    public final String XML_CONFIG = "<?xml version=\"1.0\" ?>\n" +
            "\n" +
            "<surveyor>\n" +
            "    <!--\n" +
            "   A sample data source, default might be the standard JSON data source\n" +
            "    -->\n" +
            "    <datasource type=\"LocalClientSideDataProvider\">\n" +
            "        <property name=\"fileLocation\" value=\"data/publications.json\"/>\n" +
            "        <schema>\n" +
            "            <field name=\"CLON\" type=\"CoordinateLon\"/> <!-- type would be controlled vocabulary containing: Coordinate, Date, String, Integer-->\n" +
            "            <field name=\"CLAT\" type=\"CoordinateLat\"/>\n" +
            "            <field name=\"PY\" type=\"Date\"/>\n" +
            "            <field name=\"URL\" type=\"String\"/>\n" +
            "            <field name=\"PTN\" type=\"String\"/>\n" +
            "            <field name=\"DCN\" type=\"String\"/>\n" +
            "            <field name=\"DN\" type=\"String\"/>\n" +
            "            <field name=\"STN\" type=\"String\"/>\n" +
            "            <field name=\"OTN\" type=\"String\"/>\n" +
            "            <field name=\"QI\" type=\"String\"/>\n" +
            "            <field name=\"FA\" type=\"String\"/>\n" +
            "            <field name=\"TTL\" type=\"String\"/>\n" +
            "            <field name=\"PUB\" type=\"String\"/>\n" +
            "            <field name=\"CN\" type=\"String\"/>\n" +
            "            <field name=\"FR\" type=\"Integer\"/>\n" +
            "            <field name=\"DSN\" type=\"String\"/>\n" +
            "        </schema>\n" +
            "    </datasource>\n" +
            "\n" +
            "    <views>\n" +
            "        <map name=\"Medicine Quality Map\" maxZoomOutLevel=\"2\" range=\"World\"> <!-- alternative type may be tabular data -->\n" +
            "            <!-- Assuming the range is a enumerated list of places, like World, Asia, Africa, Europe etc-->\n" +
            "            <marker>\n" +
            "                <lonField fieldName=\"CLON\"/>\n" +
            "                <latField fieldName=\"CLAT\"/>\n" +
            "            </marker>\n" +
            "            <legend relativeImagePath=\"images/LegendQbL.png\" positionFromTopInPixels=\"250\"/>\n" +
            "            <label>\n" +
            "            <![CDATA[\n" +
            "            <strong>Medicine Quality Map</strong><p> The Medicine Quality Map tab shows a single pin for each study with\n" +
            "            antimalarial quality data for that country or location. Pins are associated with tabular text explaining \n" +
            "            each selected survey. The pin colour represents the maximum medicine failure rate reported in the survey. \n" +
            "            Once a pin is clicked the number of reports found in the country of the selected pin will be listed below \n" +
            "            the map. Pins associated with the same location will be spread so the second and subsequent pins at the \n" +
            "            same location will be shown at a slightly different locations.<br><br>\n" +
            "            <span style=\"color:#D80009\">\n" +
            "            <strong>Warning</strong> - the pin colour represents the maximum failure rate for the\n" +
            "            medicine with the highest failure rate, not an average. Other medicines, if\n" +
            "            assayed, will therefore be of better quality.<br><br>These data cannot, because of the sampling methodology \n" +
            "            used in most of the\n" +
            "            individual studies, be used to give aggregated estimates of the percentage\n" +
            "            of antimalarials in individual countries or globally that are poor quality.\n" +
            "            As more objective data becomes available we hope that this will be possible.\n" +
            "            </span></p>\n" +
            "            ]]>\n" +
            "            </label>" +
            "        </map>\n" +
            "        <table name=\"Report table\">\n" +
            "            <columns sortOnColumn=\"PY\">\n" +
            "                <column fieldName=\"FA\" fieldTitle=\"1st Author\"/>\n" +
            "                <column fieldName=\"PY\" fieldTitle=\"Year\" />\n" +
            "                <column fieldName=\"TTL\" fieldTitle=\"Title\" hyperlinkField=\"URL\"/>\n" +
            "                <column fieldName=\"PUB\" fieldTitle=\"Publication\"/>\n" +
            "                <column fieldName=\"CN\" fieldTitle=\"Countries\"/>\n" +
            "            </columns>\n" +
            "        </table>\n" +
            "    </views>\n" +
            "\n" +
            "    <filters>\n" +
            "        <label>\n" +
            "            <![CDATA[\n" +
            "            Select filters:\n" +
            "            <br/>\n" +
            "            Use the filters below to selected publications or survey rows,\n" +
            "            <br/> according to Medicine, Report type, Sampling type, Medicine source,\n" +
            "            <br/> and Quality issue.\n" +
            "            ]]>\n" +
            "        </label>\n " +
            "        <filterMultipleFields fields=\"DN, DCN\" name=\"Medicines\">\n" +
            "            <label>\n" +
            "                Allows the user to select the medicines, based on International Nonproprietary Names (INN)\n" +
            "                or categories of medicines. Medicines are classified in three categories Artemisinin derivatives\n" +
            "                (including all the artemisinin derivative monotherapies), Artemisinin based Combination Therapies (ACTs)\n" +
            "                and Non-artemisinins. These categories are mutually exclusive and individual drugs or combinations are\n" +
            "                listed below. The selection of all publications that contain information on each individual medicine or\n" +
            "                medicine categories can be obtained by filtering.\n" +
            "            </label>\n" +
            "            <filterValueLabelMap>\n" +
            "                <filterLabel fieldValue=\"All\">All drugs and combination therapies</filterLabel>\n" +
            "                <filterLabel fieldValue=\"ACT\">Artemisinin Combination Therapies</filterLabel>\n" +
            "            </filterValueLabelMap>\n" +
            "        </filterMultipleFields>\n" +
            "        <filter field=\"PTN\" name=\"Report type\">\n" +
            "            <label>\n" +
            "                Lists the different types of publication found describing antimalarial drug quality.\n" +
            "                Papers on techniques, drug legislation, reviews, and other reports usually\n" +
            "                do not contain location information and therefore will not appear in the map.\n" +
            "                They can however be found in the report table.\n" +
            "            </label>\n" +
            "            <filterValueLabelMap>\n" +
            "                <filterLabel fieldValue=\"All\">Any type of report</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Non peer reviewed article\">Publications that have not undergone peer review</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Peer reviewed article\">Articles published in a peer reviewed journal</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Public alert\">Health warnings and articles issued by National Medicines Regulatory Agencies</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Lay press\">Articles published in newspapers or magazines, both as paper copies and online, about seizures or recalls</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Report\">Reports of surveys by National and International Organizations not published on scientific journals</filterLabel>\n" +
            "                <filterLabel fieldValue=\"PhD thesis\">PhD thesis on the quality of antimalarials</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Analysis techniques\">Articles describing analytical techniques for quality testing and sampling techniques</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Reviews\">Compilation of peer review articles of counterfeit antimalarials or anti-infectives. No sample collection conducted</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Drug regulation &amp; definitions\">Articles regarding antimalarial medicine regulation and/or definitions</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Other\">Articles related to antimalarial quality but without sample collection</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Short Communication/Note\">Posters, notes or short communications about antimalarial quality</filterLabel>\n" +
            "            </filterValueLabelMap>\n" +
            "        </filter>\n" +
            "        <filter field=\"STN\" name=\"Collection type\">\n" +
            "            <label>\n" +
            "                Lists the type of sampling methodology used in each report.\n" +
            "                Only studies with evidence describing how randomisation was performed have been included as 'Random Survey'\n" +
            "            </label>\n" +
            "            <filterValueLabelMap>\n" +
            "                <filterLabel fieldValue=\"Convenience survey\">Surveys with sample collection not conducted in a random manner</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Case reports\">Studies describing patients not responding to antimalarials. Recalls of antimalarial or non-MRA public health warnings are also included under this category</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Random survey\">Studies in which outlet and/or sample selection was conducted in a  random manner with description of how the randomization was performed</filterLabel>\n" +
            "                <filterLabel fieldValue=\"MRA seizure\">Reports describing antimalarial confiscations or warnings by police or MRAs</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Unknown\">Studies that do not specify the methodology followed for sample collection.</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Bioavailability studies\">Studies describing in vivo comparative bioavailability testing for adequate antimalarial levels for treatment. Measurement of the rate and extent to which a drug reaches the systemic circulation.</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Laboratory assembled collections\">Studies describing samples that were put together in laboratories to answer chemical rather than epidemiological questions</filterLabel>\n" +
            "            </filterValueLabelMap>\n" +
            "\n" +
            "    </filter>\n" +
            "        <filter field=\"QI\" name=\"Quality classification\">\n" +
            "            <label>\n" +
            "                We use, unless otherwise specified in the report, the term 'falsified' as a synonym for\n" +
            "                counterfeit or spurious, referring to a medical\n" +
            "                product produced with criminal intent to mislead, without reference to intellectual\n" +
            "                property concerns. If authors did not examine packaging\n" +
            "                we class samples that failed chemical assays, but without detected\n" +
            "                wrong active ingredients, as poor quality and not falsified/counterfeit.\n" +
            "                Samples that did not fail chemical assays and/or a packaging tests are considered as good quality.\n" +
            "                Samples are only classified as 'Good Quality' if all samples collected were good quality.\n" +
            "                Samples classified as poor quality, falsified or substandard may contain good quality medicines.\n" +
            "            </label>\n" +
            "            <filterValueLabelMap>\n" +
            "                <filterLabel fieldValue=\"Falsified\">Samples with fake packaging + right amount of API or wrong or none or incorrect API. Also those samples with no genuine packaging to check + wrong or no Active Pharmaceutical Ingredient</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Good Quality\">Set of samples that did not fail chemical assays and packaging tests</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Substandard\">Samples with genuine packaging + incorrect quantity of correct API</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Poor Quality\">‘Samples without reference packaging available for comparison, containing incorrect quantities (>zero %) of the correct API or failure to comply with other quality specifications (e.g. dissolution tests, contents of impurity, sterility etc)</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Degraded\">Genuine packaging + correct quantity of correct API but chemical evidence of degradation</filterLabel>\n" +
            "            </filterValueLabelMap>\n" +
            "        </filter>\n" +
            "        <filter field=\"OTN\" name=\"Medicine Source\">\n" +
            "            <label>\n" +
            "                Lists the type of outlet where the collection of samples was\n" +
            "                performed. Reports that do not provide clear information about the drug provider\n" +
            "                will be classified as unknown.\n" +
            "            </label>\n" +
            "            <filterValueLabelMap>\n" +
            "                <filterLabel fieldValue=\"Unknown\">Studies where the source of samples is unknown</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Hospital pharmacy\">Samples collected only in public or private sector hospitals/hospital pharmacies</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Private pharmacy\">Samples collected only in private sector pharmacies</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Manufacturing company\">Samples provided from the manufacturing company</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Distributor/ agent/ trader/ wholesale pharmacies\">Samples provided from distributor, agent, trader or wholesale pharmacies</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Clinic\">Samples collected from a private sector clinic</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Trader\">Yes, would group under agent</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Govt &amp; Private outlets\">Samples collected from private and public sectors</filterLabel>\n" +
            "                <filterLabel fieldValue=\"Wholesale pharmacies\">Samples collected from wholesale pharmacies</filterLabel>\n" +
            "            </filterValueLabelMap>\n" +
            "        </filter>\n" +
            "        <filterByDateRange field=\"PY\" name=\"Filter by year\" startDate=\"1975\" endDate=\"currentYear\">\n" +
            "            <label>\n" +
            "                Filter studies by publication year range: 1975 to 2013\n" +
            "            </label>\n" +
            "        </filterByDateRange>\n" +
            "    </filters>\n" +
            "\n" +
            "</surveyor>\n";

    public final String JSON_DATA_SOURCE = "[" +
            "{\"PID\":122,\"DOI\":\"\",\"TTL\":\"Pharmacia Withdraws Metakelfin in White Sachets\",\"FA\":\"Modern Ghana News\",\"PY\":2002,\"PUB\":\"modernghana.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.modernghana.com/news/20469/1/pharmacia-withdraws-metakelfin-in-white-sachets.html\",\"DID\":39,\"DSN\":\"SMP+PYR\",\"DN\":\"Sulfamethoxypyrazine-Pyrimethamine\",\"CID\":29,\"CN\":\"Ghana\",\"CLAT\":5.55,\"CLON\":-0.25,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":471,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":2,\"STN\":\"Case reports\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"17/02/2002\",\"ED\":\"\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":135,\"DOI\":\"\",\"TTL\":\"NAFDAC raises alarm over circulation of adulterated drugs in Ondo\",\"FA\":\"Oladoyinbo, Y.\",\"PY\":2011,\"PUB\":\"Nigerian Tribune\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":30,\"DSN\":\"AL\",\"DN\":\"Artemether-Lumefantrine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":98,\"LN\":\"Akure & Owo, in Ondo State\",\"LLAT\":7.25,\"LLON\":5.195,\"SDI\":505,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/10/2011\",\"ED\":\"31/10/2011\",\"ICR\":\"-1\",\"DCN\":\"ACT\",\"NST\":-1}," +
            "{\"PID\":134,\"DOI\":\"\",\"TTL\":\"3 held over fake malaria drugs\",\"FA\":\"Bagala, A.\",\"PY\":2008,\"PUB\":\"Daily Monitor\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.monitor.co.ug/News/Education/-/688336/759052/-/view/printVersion/-/13hs21sz/-/index.html\",\"DID\":23,\"DSN\":\"TET\",\"DN\":\"Tetracycline\",\"CID\":24,\"CN\":\"Uganda\",\"CLAT\":0.313611,\"CLON\":32.5811,\"LID\":97,\"LN\":\"Kireka, Wakiso District\",\"LLAT\":0.6,\"LLON\":32.58333,\"SDI\":503,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/10/2008\",\"ED\":\"31/10/2008\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":133,\"DOI\":\"\",\"TTL\":\"Fake drugs hit Ugandan market\",\"FA\":\"Mugabe, D.\",\"PY\":2009,\"PUB\":\"The New Vision Online\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":1,\"DSN\":\"AQ\",\"DN\":\"Amodiaquine\",\"CID\":24,\"CN\":\"Uganda\",\"CLAT\":0.313611,\"CLON\":32.5811,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":501,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/01/2009\",\"ED\":\"31/12/2009\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":133,\"DOI\":\"\",\"TTL\":\"Fake drugs hit Ugandan market\",\"FA\":\"Mugabe, D.\",\"PY\":2009,\"PUB\":\"The New Vision Online\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":21,\"DSN\":\"QN\",\"DN\":\"Quinine\",\"CID\":24,\"CN\":\"Uganda\",\"CLAT\":0.313611,\"CLON\":32.5811,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":500,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/01/2009\",\"ED\":\"31/12/2009\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":229,\"DOI\":\"\",\"TTL\":\"Child dead, 104 sick after taking malaria medication in SW China\",\"FA\":\"People´s Daily Online.com\",\"PY\":2010,\"PUB\":\"People´s Daily Online.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://english.people.com.cn/90001/90776/90882/7066554.html\",\"DID\":8,\"DSN\":\"CQ\",\"DN\":\"Chloroquine\",\"CID\":49,\"CN\":\"China\",\"CLAT\":39.9139,\"CLON\":116.392,\"LID\":110,\"LN\":\"Dongxing district of Neijiang city\",\"LLAT\":29.58274,\"LLON\":105.0598,\"SDI\":548,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":2,\"STN\":\"Case reports\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"11/05/2010\",\"ED\":\"11/05/2010\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":122,\"DOI\":\"\",\"TTL\":\"Pharmacia Withdraws Metakelfin in White Sachets\",\"FA\":\"Modern Ghana News\",\"PY\":2002,\"PUB\":\"modernghana.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.modernghana.com/news/20469/1/pharmacia-withdraws-metakelfin-in-white-sachets.html\",\"DID\":39,\"DSN\":\"SMP+PYR\",\"DN\":\"Sulfamethoxypyrazine-Pyrimethamine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":472,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":2,\"STN\":\"Case reports\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"17/02/2002\",\"ED\":\"\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":121,\"DOI\":\"\",\"TTL\":\"Announcement on fake (imitation) drugs\",\"FA\":\"Ministry  of Health & National Drug Law and Notifications, Burma\",\"PY\":2001,\"PUB\":\"mission.myanmar online\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":2,\"DSN\":\"AM\",\"DN\":\"Artemether\",\"CID\":4,\"CN\":\"Myanmar/Burma\",\"CLAT\":19.75,\"CLON\":96.1,\"LID\":94,\"LN\":\"Yangon\",\"LLAT\":16.8,\"LLON\":96.15,\"SDI\":470,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"08/11/2001\",\"ED\":\"\",\"ICR\":\"-1\",\"DCN\":\"Artemisinin Derivatives\",\"NST\":1}," +
            "{\"PID\":119,\"DOI\":\"\",\"TTL\":\"Nigeria: Nafdac Impounds N10 Million Fake Malaria Drugs\",\"FA\":\"Udoh, F.\",\"PY\":2010,\"PUB\":\"AllAfrica.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":29,\"DSN\":\"AL\",\"DN\":\"Artemether-Lumefantrine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":55,\"LN\":\"Lagos\",\"LLAT\":6.43918,\"LLON\":3.42348,\"SDI\":468,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":3,\"QI\":\"Poor Quality\",\"FR\":100,\"SD\":\"13/01/2010\",\"ED\":\"13/01/2010\",\"ICR\":\"-1\",\"DCN\":\"ACT\",\"NST\":9}," +
            "{\"PID\":85,\"DOI\":\"\",\"TTL\":\"Fake quinine on market\",\"FA\":\"Bogere, H. & Nafula, J.\",\"PY\":2007,\"PUB\":\"Daily Monitor\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://allafrica.com/stories/200705090881.html\",\"DID\":21,\"DSN\":\"QN\",\"DN\":\"Quinine\",\"CID\":24,\"CN\":\"Uganda\",\"CLAT\":0.313611,\"CLON\":32.5811,\"LID\":52,\"LN\":\"Kampala\",\"LLAT\":0.312965,\"LLON\":32.5881,\"SDI\":343,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"\",\"ED\":\"\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":118,\"DOI\":\"\",\"TTL\":\"Nigeria: NAFDAC Impounds Consignments of Fake Drugs Valued At N500 Million\",\"FA\":\"Udoh, F.\",\"PY\":2010,\"PUB\":\"AllAfrica.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":29,\"DSN\":\"AL\",\"DN\":\"Artemether-Lumefantrine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":55,\"LN\":\"Lagos\",\"LLAT\":6.43918,\"LLON\":3.42348,\"SDI\":466,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"10/06/2010\",\"ED\":\"10/06/2010\",\"ICR\":\"-1\",\"DCN\":\"ACT\",\"NST\":-1}," +
            "{\"PID\":133,\"DOI\":\"\",\"TTL\":\"Fake drugs hit Ugandan market\",\"FA\":\"Mugabe, D.\",\"PY\":2009,\"PUB\":\"The New Vision Online\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":39,\"DSN\":\"SMP+PYR\",\"DN\":\"Sulfamethoxypyrazine-Pyrimethamine\",\"CID\":24,\"CN\":\"Uganda\",\"CLAT\":0.313611,\"CLON\":32.5811,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":502,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/01/2009\",\"ED\":\"31/12/2009\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":229,\"DOI\":\"\",\"TTL\":\"Child dead, 104 sick after taking malaria medication in SW China\",\"FA\":\"People´s Daily Online.com\",\"PY\":2010,\"PUB\":\"People´s Daily Online.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://english.people.com.cn/90001/90776/90882/7066554.html\",\"DID\":17,\"DSN\":\"PQ\",\"DN\":\"Primaquine\",\"CID\":49,\"CN\":\"China\",\"CLAT\":39.9139,\"CLON\":116.392,\"LID\":110,\"LN\":\"Dongxing district of Neijiang city\",\"LLAT\":29.58274,\"LLON\":105.0598,\"SDI\":549,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":2,\"STN\":\"Case reports\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"11/05/2010\",\"ED\":\"11/05/2010\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":242,\"DOI\":\"\",\"TTL\":\"NAFDAC uncovers illegal drug factory in Onitsha\",\"FA\":\"Vanguardngr.com\",\"PY\":2012,\"PUB\":\"Vanguardngr.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.vanguardngr.com/2012/05/nafdac-uncovers-illegal-drug-factory-in-onitsha/\",\"DID\":21,\"DSN\":\"QN\",\"DN\":\"Quinine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":71,\"LN\":\"Onitsha, Anambra State\",\"LLAT\":6.13506,\"LLON\":6.771505,\"SDI\":550,\"OTI\":3,\"OTN\":\"Shop\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"\",\"ED\":\"\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":680}," +
            "{\"PID\":186,\"DOI\":\"\",\"TTL\":\"NAFDAC swoops on fake drugs stores in Benue\",\"FA\":\"Nigerian Tribune\",\"PY\":2011,\"PUB\":\"Nigerian Tribune\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":4,\"DSN\":\"AS\",\"DN\":\"Artesunate\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":99,\"LN\":\"Makurdi, Benue state\",\"LLAT\":7.730556,\"LLON\":8.536111,\"SDI\":513,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"13/01/2011\",\"ED\":\"13/01/2011\",\"ICR\":\"-1\",\"DCN\":\"Artemisinin Derivatives\",\"NST\":-1}," +
            "{\"PID\":186,\"DOI\":\"\",\"TTL\":\"NAFDAC swoops on fake drugs stores in Benue\",\"FA\":\"Nigerian Tribune\",\"PY\":2011,\"PUB\":\"Nigerian Tribune\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":29,\"DSN\":\"AL\",\"DN\":\"Artemether-Lumefantrine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":99,\"LN\":\"Makurdi, Benue state\",\"LLAT\":7.730556,\"LLON\":8.536111,\"SDI\":514,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"13/01/2011\",\"ED\":\"13/01/2011\",\"ICR\":\"-1\",\"DCN\":\"ACT\",\"NST\":-1}," +
            "{\"PID\":186,\"DOI\":\"\",\"TTL\":\"NAFDAC swoops on fake drugs stores in Benue\",\"FA\":\"Nigerian Tribune\",\"PY\":2011,\"PUB\":\"Nigerian Tribune\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":27,\"DSN\":\"SP\",\"DN\":\"Sulphadoxine-Pyrimethamine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":99,\"LN\":\"Makurdi, Benue state\",\"LLAT\":7.730556,\"LLON\":8.536111,\"SDI\":515,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"13/01/2011\",\"ED\":\"13/01/2011\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":187,\"DOI\":\"\",\"TTL\":\"NAFDAC´s unfinished business in Kaduna\",\"FA\":\"Nigerian Tribune\",\"PY\":2011,\"PUB\":\"Nigerian Tribune\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":29,\"DSN\":\"AL\",\"DN\":\"Artemether-Lumefantrine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":100,\"LN\":\"Kaduna\",\"LLAT\":10.51667,\"LLON\":7.433333,\"SDI\":516,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"15/02/2010\",\"ED\":\"15/02/2010\",\"ICR\":\"-1\",\"DCN\":\"ACT\",\"NST\":-1}," +
            "{\"PID\":118,\"DOI\":\"\",\"TTL\":\"Nigeria: NAFDAC Impounds Consignments of Fake Drugs Valued At N500 Million\",\"FA\":\"Udoh, F.\",\"PY\":2010,\"PUB\":\"AllAfrica.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":4,\"DSN\":\"AS\",\"DN\":\"Artesunate\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":55,\"LN\":\"Lagos\",\"LLAT\":6.43918,\"LLON\":3.42348,\"SDI\":467,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"10/06/2010\",\"ED\":\"10/06/2010\",\"ICR\":\"-1\",\"DCN\":\"Artemisinin Derivatives\",\"NST\":-1}," +
            "{\"PID\":123,\"DOI\":\"\",\"TTL\":\"Beware of Fake Co-Artem Malaria Tabs On the Market\",\"FA\":\"Hope, K.E.\",\"PY\":2009,\"PUB\":\"The Ghanaian Times\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":29,\"DSN\":\"AL\",\"DN\":\"Artemether-Lumefantrine\",\"CID\":29,\"CN\":\"Ghana\",\"CLAT\":5.55,\"CLON\":-0.25,\"LID\":68,\"LN\":\"Kumasi\",\"LLAT\":6.68711,\"LLON\":-1.62196,\"SDI\":473,\"OTI\":7,\"OTN\":\"Distributor/agent/trader/wholesale pharmacies\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"10/07/2009\",\"ED\":\"\",\"ICR\":\"-1\",\"DCN\":\"ACT\",\"NST\":2}," +
            "{\"PID\":134,\"DOI\":\"\",\"TTL\":\"3 held over fake malaria drugs\",\"FA\":\"Bagala, A.\",\"PY\":2008,\"PUB\":\"Daily Monitor\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.monitor.co.ug/News/Education/-/688336/759052/-/view/printVersion/-/13hs21sz/-/index.html\",\"DID\":39,\"DSN\":\"SMP+PYR\",\"DN\":\"Sulfamethoxypyrazine-Pyrimethamine\",\"CID\":24,\"CN\":\"Uganda\",\"CLAT\":0.313611,\"CLON\":32.5811,\"LID\":97,\"LN\":\"Kireka, Wakiso District\",\"LLAT\":0.6,\"LLON\":32.58333,\"SDI\":504,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/10/2008\",\"ED\":\"31/10/2008\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":215,\"DOI\":\"\",\"TTL\":\"Tru Scan: NAFDAC seizes counterfeit medicines in Nasarawa\",\"FA\":\"Dailytrust.com\",\"PY\":2012,\"PUB\":\"Dailytrust.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.dailytrust.com.ng/index.php?option=com_content&view=article&id=158688:tru-scan-nafdac-seizes-counterfeit-medicines-in-nasarawa&catid=1:news&Itemid=2\",\"DID\":27,\"DSN\":\"SP\",\"DN\":\"Sulphadoxine-Pyrimethamine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":109,\"LN\":\"Nasarawa\",\"LLAT\":8.57962,\"LLON\":8.29716,\"SDI\":546,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":2,\"STN\":\"Case reports\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"02/04/2012\",\"ED\":\"02/04/2012\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":84,\"DOI\":\"\",\"TTL\":\"Belgian customs seize record haul of fake pills, from India\",\"FA\":\"Africasia.com\",\"PY\":2008,\"PUB\":\"Africasia.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.industryweek.com/articles/belgian_customs_seize_record_haul_of_fake_pills_from_india_17446.aspx\",\"DID\":27,\"DSN\":\"SP\",\"DN\":\"Sulphadoxine-Pyrimethamine\",\"CID\":96,\"CN\":\"Belgium\",\"CLAT\":50.85034,\"CLON\":4.35171,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":342,\"OTI\":0,\"OTN\":\"Unknown\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"\",\"ED\":\"\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":127,\"DOI\":\"\",\"TTL\":\"World Pharma: A potential massacre\",\"FA\":\"The Economist Intelligence Unit\",\"PY\":2009,\"PUB\":\"The Economist Intelligence Unit\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://viewswire.eiu.com/index.asp?layout=ib3Article&article_id=1685058953&pubtypeid=1152462500&country_id=1510000351&IBNL=true&rf=0\",\"DID\":39,\"DSN\":\"SMP+PYR\",\"DN\":\"Sulfamethoxypyrazine-Pyrimethamine\",\"CID\":35,\"CN\":\"United Republic of Tanzania\",\"CLAT\":-6.17306,\"CLON\":35.7419,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":483,\"OTI\":11,\"OTN\":\"Govt & Private outlets\",\"STI\":2,\"STN\":\"Case reports\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/01/2009\",\"ED\":\"31/12/2009\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":128,\"DOI\":\"\",\"TTL\":\"FDB recalls Counterfeit and substandard anti-malarial medicines\",\"FA\":\"Ghana News Agency\",\"PY\":2010,\"PUB\":\"Ghanaweb.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.ghanaweb.com/GhanaHomePage/health/artikel.php?ID=197020#\",\"DID\":39,\"DSN\":\"SMP+PYR\",\"DN\":\"Sulfamethoxypyrazine-Pyrimethamine\",\"CID\":29,\"CN\":\"Ghana\",\"CLAT\":5.55,\"CLON\":-0.25,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":484,\"OTI\":11,\"OTN\":\"Govt & Private outlets\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/01/2010\",\"ED\":\"31/12/2010\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":128,\"DOI\":\"\",\"TTL\":\"FDB recalls Counterfeit and substandard anti-malarial medicines\",\"FA\":\"Ghana News Agency\",\"PY\":2010,\"PUB\":\"Ghanaweb.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.ghanaweb.com/GhanaHomePage/health/artikel.php?ID=197020#\",\"DID\":4,\"DSN\":\"AS\",\"DN\":\"Artesunate\",\"CID\":29,\"CN\":\"Ghana\",\"CLAT\":5.55,\"CLON\":-0.25,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":485,\"OTI\":11,\"OTN\":\"Govt & Private outlets\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/01/2010\",\"ED\":\"31/12/2010\",\"ICR\":\"-1\",\"DCN\":\"Artemisinin Derivatives\",\"NST\":-1}," +
            "{\"PID\":128,\"DOI\":\"\",\"TTL\":\"FDB recalls Counterfeit and substandard anti-malarial medicines\",\"FA\":\"Ghana News Agency\",\"PY\":2010,\"PUB\":\"Ghanaweb.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.ghanaweb.com/GhanaHomePage/health/artikel.php?ID=197020#\",\"DID\":21,\"DSN\":\"QN\",\"DN\":\"Quinine\",\"CID\":29,\"CN\":\"Ghana\",\"CLAT\":5.55,\"CLON\":-0.25,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":486,\"OTI\":11,\"OTN\":\"Govt & Private outlets\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"01/01/2010\",\"ED\":\"31/12/2010\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}," +
            "{\"PID\":128,\"DOI\":\"\",\"TTL\":\"FDB recalls Counterfeit and substandard anti-malarial medicines\",\"FA\":\"Ghana News Agency\",\"PY\":2010,\"PUB\":\"Ghanaweb.com\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"http://www.ghanaweb.com/GhanaHomePage/health/artikel.php?ID=197020#\",\"DID\":8,\"DSN\":\"CQ\",\"DN\":\"Chloroquine\",\"CID\":29,\"CN\":\"Ghana\",\"CLAT\":5.55,\"CLON\":-0.25,\"LID\":-1,\"LN\":\"\",\"LLAT\":0.0,\"LLON\":0.0,\"SDI\":487,\"OTI\":11,\"OTN\":\"Govt & Private outlets\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":2,\"QI\":\"Substandard\",\"FR\":100,\"SD\":\"01/01/2010\",\"ED\":\"31/12/2010\",\"ICR\":\"-1\",\"DCN\":\"Non-artemisinins\",\"NST\":-1}" +
            "]";
}

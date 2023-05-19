/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/

import fs from 'fs';
import path from 'path';
import { Test } from './test';
import { TestData } from '../types';

/** Base HTML template for the report */
const HTML_TEMPLATE = `<!DOCTYPE html>
<html>
  <head>
    <title>Baremaps Renderer</title>
    <style>
      :root {
        --color-pass: rgb(40, 156, 40);
        --color-pass-transparent: rgba(40, 156, 40, 0.05);
        --color-error: rgb(255, 59, 59);
        --color-error-transparent: rgba(255, 59, 59, 0.05);
      }

      body {
        font-family: sans-serif;
        padding: 4rem 8rem;
      }

      @media (max-width: 768px) {
        body {
          padding: 4rem 0.5rem;
        }

        .results {
          gap: 0.5rem !important;
        }
      }

      .title {
        margin-bottom: 3rem;
      }

      h1 {
        font-size: 3rem;
        margin-top: 0;
        margin-bottom: 1rem;
      }

      h2 {
        font-size: 1.5rem;
        margin-top: 0;
        margin-bottom: 1rem;
        text-transform: uppercase;
        font-weight: 500;
      }

      h3 {
        font-size: 2rem;
        margin: 1rem 0;
      }

      h4 {
        font-size: 1.25rem;
        margin-bottom: 0;
      }

      h4.fail {
        margin-top: 0;
        text-transform: uppercase;
        color: var(--color-error);
      }

      h4.pass {
        margin-top: 0;
        text-transform: uppercase;
        color: var(--color-pass);
      }

      a {
        position: relative;
        text-decoration: underline;
        color: inherit;
      }

      pre {
        font-size: 1.25rem;
      }

      .results {
        display: flex;
        flex-direction: column;
        gap: 2rem;
      }

      .summary {
        border-left: 3px solid black;
        padding: 1rem 2rem;
        background-color: rgba(0, 0, 0, 0.05);
      }

      .summary h4 {
        text-transform: uppercase;
        margin-top: 1rem;
      }

      .p-pass {
        color: var(--color-pass);
        font-weight: bold;
      }

      .p-fail {
        color: var(--color-error);
        font-weight: bold;
      }

      .result {
        padding: 1rem 2rem;
      }
      
      .result.fail {
        border-left: 3px solid var(--color-error);
        background-color: var(--color-error-transparent);
      }

      .result.pass {
        border-left: 3px solid var(--color-pass);
        background-color: var(--color-pass-transparent);
      }

      pre {
        margin: 0;
      }

      .images {
        display: grid;
        grid-template-columns: 1fr 1fr 1fr;
        gap: 1rem;
      }

      img {
        width: 100%;
        aspect-ratio: 1;
      }
    </style>
  </head>
  <body>
    <div class="title">
      <h1>Baremaps Renderer</h1>
      <h2>Integration Testing Report</h2>
    </div>
    <div class="results">
      <div class="summary">
        <h4>Summary</h4>
        <p>Out of <strong>{{ TOT_TESTS }}</strong> tests:</p>
        <ul>
          <li><span class="p-pass">{{ TOT_PASS_TESTS }} tests passed</span></li>
          <li><span class="p-fail">{{ TOT_FAIL_TESTS }} tests failed</span></li>
        </ul>
      </div>
      {{ TESTS }}
    </div>
  </body>
</html>
`;

/** HTML template for a single failed test */
const FAILED_TEST_TEMPLATE = `<div class="result fail">
    <h4 class="fail">FAILED</h4>
    <h3>{{ TEST_NAME }}</h3>
    <p>
        {{ TEST_PATH }}
    </p>
    <pre>
        <code>
{{ METADATA }}</code>
    </pre>
    <div class="images">
        <h4>Expected</h4>
        <h4>Actual</h4>
        <h4>Difference</h4>
        <img src="{{ EXPECTED_IMG_PATH }}" />
        <img src="{{ ACTUAL_IMG_PATH }}" />
        <img src="{{ DIFF_IMG_PATH }}" />
    </div>
</div>`;

/** HTML template for a single passed test */
const PASSED_TEST_TEMPLATE = `<div class="result pass">
    <h4 class="pass">PASSED</h4>
    <h3>{{ TEST_NAME }}</h3>
    <p>
        {{ TEST_PATH }}
    </p>
    <pre>
        <code>
{{ METADATA }}</code>
    </pre>
    <div class="images">
        <h4>Expected</h4>
        <h4>Actual</h4>
        <h4>Difference</h4>
        <img src="{{ EXPECTED_IMG_PATH }}" />
        <img src="{{ ACTUAL_IMG_PATH }}" />
        <img src="{{ DIFF_IMG_PATH }}" />
    </div>
</div>`;

/**
 * ReportGenerator class
 *
 * Generates an HTML report from generated images during tests
 */
export class ReportGenerator {
  public static readonly reportFilename = 'report.html';

  private testFolder: string;
  private tests: Test[];

  /**
   * Creates a new ReportGenerator
   *
   * @param testFolder Path to the test folder
   * @param tests Array of tests
   * @returns A new ReportGenerator
   */
  constructor(testFolder: string, tests: Test[]) {
    this.testFolder = testFolder;
    if (!path.isAbsolute(this.testFolder)) {
      this.testFolder = path.join(process.cwd(), this.testFolder);
    }
    this.tests = tests;
  }

  /** Generates and saves the HTML report */
  public generate() {
    const baseHtml = this.formatHtmlBase();

    const testsData = this.tests
      .map((test) => this.getTestData(test))
      .filter((test) => test !== null);

    const testsHtml = testsData
      .map((testData) => this.formatHtmlTest(testData!))
      .join('\n');

    const html = baseHtml.replace('{{ TESTS }}', testsHtml);
    this.writeHtml(html);

    console.log(
      "INFO: Report generated at '" +
        path.join(this.testFolder, ReportGenerator.reportFilename) +
        "'",
    );
  }

  /**
   * Returns an object with the test images paths and metadata for a given test
   *
   * @param test The test to get the data from
   * @returns An object with the test images paths and metadata
   */
  private getTestData(test: Test): TestData | null {
    // return if the test is not completed
    if (test.success === undefined) {
      return null;
    }
    // check if the images exist
    if (
      !fs.existsSync(path.join(test.testPath, Test.expectedFilename)) ||
      !fs.existsSync(path.join(test.testPath, Test.actualFilename)) ||
      !fs.existsSync(path.join(test.testPath, Test.diffFilename))
    ) {
      console.log(
        'WARN: Missing images for test ' + test.testPath + ', skipping...',
      );
      return null;
    }
    const testName = path.basename(test.testPath);
    const testsRelativePath = path.relative(this.testFolder, test.testPath);
    return {
      path: test.testPath,
      name: testName,
      metadata: test.metadata,
      sucess: test.success,
      expectedImagePath: path.join(testsRelativePath, Test.expectedFilename),
      actualImagePath: path.join(testsRelativePath, Test.actualFilename),
      diffImagePath: path.join(testsRelativePath, Test.diffFilename),
    };
  }

  /**
   * Creates an object with the summary of the tests
   *
   * @returns An object with the summary of the tests
   */
  private getSummary() {
    const failedTests = this.tests.filter((t) => t.success === false);
    const summary = {
      total: this.tests.length,
      failed: failedTests.length,
      passed: this.tests.length - failedTests.length,
    };
    return summary;
  }

  /**
   * Returns the HTML template with the summary of the tests
   *
   * @returns The HTML template with the summary of the tests
   */
  private formatHtmlBase() {
    const summary = this.getSummary();
    return HTML_TEMPLATE.replace('{{ TOT_TESTS }}', summary.total.toString())
      .replace('{{ TOT_PASS_TESTS }}', summary.passed.toString())
      .replace('{{ TOT_FAIL_TESTS }}', summary.failed.toString());
  }

  /**
   * Returns the HTML template for a single test
   *
   * @param testData The data spec for a test
   * @returns The HTML template for a single test
   */
  private formatHtmlTest(testData: TestData) {
    let template;
    if (testData.sucess) {
      template = PASSED_TEST_TEMPLATE;
    } else {
      template = FAILED_TEST_TEMPLATE;
    }
    return template
      .replace('{{ TEST_NAME }}', testData.name)
      .replace('{{ TEST_PATH }}', testData.path)
      .replace('{{ METADATA }}', JSON.stringify(testData.metadata, null, 4))
      .replace('{{ EXPECTED_IMG_PATH }}', testData.expectedImagePath)
      .replace('{{ ACTUAL_IMG_PATH }}', testData.actualImagePath)
      .replace('{{ DIFF_IMG_PATH }}', testData.diffImagePath);
  }

  /**
   * Writes the HTML report to disk
   *
   * @param html The HTML report
   */
  private writeHtml(html: string) {
    fs.writeFileSync(
      path.join(this.testFolder, ReportGenerator.reportFilename),
      html,
    );
  }
}

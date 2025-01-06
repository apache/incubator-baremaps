-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to you under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- Converts a string to a number
CREATE
    OR REPLACE FUNCTION convert_to_number(
        input_string text,
        default_value NUMERIC
    ) RETURNS NUMERIC AS $$ DECLARE RESULT NUMERIC;

BEGIN -- Replace comma with dot

input_string := REPLACE(
    input_string,
    ',',
    '.'
);

-- Use a regular expression to extract the first number from the string

input_string := SUBSTRING( input_string FROM '^[0-9]+\.?[0-9]*' );

-- Convert the extracted string to a numeric type
RESULT := input_string::NUMERIC;

IF RESULT IS NULL THEN RETURN default_value;
END IF;

RETURN RESULT;

EXCEPTION
WHEN OTHERS THEN -- Return the default value in case of any error
RETURN default_value;
END;

$$ LANGUAGE plpgsql;

-- Test cases for the convert_to_number function
SELECT
    assert_equals(
        convert_to_number(
            '1',
            0
        ),
        1
    );

SELECT
    assert_equals(
        convert_to_number(
            '2.3',
            0
        ),
        2.3
    );

SELECT
    assert_equals(
        convert_to_number(
            '3,4',
            0
        ),
        3.4
    );

SELECT
    assert_equals(
        convert_to_number(
            '1.5m',
            0
        ),
        1.5
    );

SELECT
    assert_equals(
        convert_to_number(
            '6.6 m',
            0
        ),
        6.6
    );

SELECT
    assert_equals(
        convert_to_number(
            'abc',
            0
        ),
        0
    );
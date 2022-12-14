import { AwsProvider } from "@cdktf/provider-aws/lib/provider";
import { App, TerraformStack } from "cdktf";
import { Construct } from "constructs";
import { DynamoDbSchema } from "./dynamodb-schema";
import { DynamodbLocalContainer } from "./local-dynamodb";

class KiteStack extends TerraformStack {
  constructor(scope: Construct, id: string) {
    super(scope, id);

    new AwsProvider(this, "AWS");

    new DynamoDbSchema(this, id, { pointInTimeRecovery: true });
  }
}

class LocalStack extends TerraformStack {
  constructor(scope: Construct, id: string) {
    super(scope, id);

    new AwsProvider(this, "AWS", {
      endpoints: [
        {
          dynamodb: "http://localhost:8000",
        },
      ],
      skipCredentialsValidation: true,
      skipMetadataApiCheck: "true",
      skipRequestingAccountId: true,
      region: "eu-north-1",
    });

    const dynamoDbLocalContainer = new DynamodbLocalContainer(
      this,
      "dynamodb-local"
    );

    new DynamoDbSchema(this, id, {
      dependsOn: [dynamoDbLocalContainer.container],
    });
  }
}

const app = new App();
new KiteStack(app, "kite");
new LocalStack(app, "local");
app.synth();

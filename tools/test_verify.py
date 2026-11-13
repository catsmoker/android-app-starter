"""Keep the reusable verification entry point bounded without invoking any build."""
import unittest
from verify import commands


class VerifyPlanTest(unittest.TestCase):
    def test_default_is_python_only(self):
        plan = commands()
        self.assertTrue(all(command[1].endswith('.py') for command in plan))
        self.assertFalse(any('gradlew' in part for command in plan for part in command))

    def test_android_plan_is_one_compile_and_selected_tests_invocation(self):
        gradle = commands(android=True)[-1]
        self.assertEqual(gradle.count(':app:compileDebugKotlin'), 1)
        self.assertEqual(gradle.count(':app:compileReleaseKotlin'), 1)
        self.assertEqual(gradle.count(':app:testDebugUnitTest'), 1)
        self.assertEqual(gradle.count('--tests'), 5)
        self.assertFalse(any(term in ' '.join(gradle) for term in ['clean', 'assemble', 'connected', 'publish', 'bundle']))


if __name__ == '__main__':
    unittest.main()
